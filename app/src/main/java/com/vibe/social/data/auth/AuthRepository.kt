package com.vibe.social.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.util.Patterns
import com.example.BuildConfig
import com.vibe.social.data.MockDataProvider
import com.vibe.social.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit

class AuthRepository private constructor() {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private var sharedPrefs: SharedPreferences? = null

    val supabaseUrl: String
        get() = try {
            BuildConfig.SUPABASE_URL
        } catch (_: Exception) {
            "https://sample-project.supabase.co"
        }

    val supabaseAnonKey: String
        get() = try {
            BuildConfig.SUPABASE_ANON_KEY
        } catch (_: Exception) {
            ""
        }

    val isSupabaseConfigured: Boolean
        get() = supabaseUrl.isNotBlank() &&
                !supabaseUrl.contains("sample-project.supabase.co") &&
                supabaseAnonKey.isNotBlank() &&
                !supabaseAnonKey.contains("dummy_anon_key")

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun initialize(context: Context) {
        sharedPrefs = context.applicationContext.getSharedPreferences("vibe_auth_prefs", Context.MODE_PRIVATE)
        restoreSession()
    }

    private fun restoreSession() {
        val prefs = sharedPrefs ?: return
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val needsProfile = prefs.getBoolean(KEY_NEEDS_PROFILE_SETUP, false)
        val userId = prefs.getString(KEY_USER_ID, null)
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val token = prefs.getString(KEY_AUTH_TOKEN, null)

        if (isLoggedIn && userId != null) {
            if (needsProfile) {
                _authState.value = AuthState.NeedsProfileSetup(email, userId, token)
            } else {
                val user = User(
                    id = userId,
                    username = prefs.getString(KEY_USERNAME, "alex_vibe") ?: "alex_vibe",
                    fullName = prefs.getString(KEY_FULL_NAME, "Alex Rivera") ?: "Alex Rivera",
                    avatarUrl = prefs.getString(
                        KEY_AVATAR_URL,
                        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80"
                    ) ?: "",
                    isVerified = prefs.getBoolean(KEY_IS_VERIFIED, false),
                    bio = prefs.getString(KEY_BIO, "") ?: "",
                    email = email
                )
                _currentUser.value = user
                MockDataProvider.currentUser = user
                _authState.value = AuthState.Authenticated(user, token)
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    suspend fun register(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        // Validation
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return@withContext AuthResult.Error("Please enter a valid email address.")
        }
        if (password.length < 6) {
            return@withContext AuthResult.Error("Password must be at least 6 characters long.")
        }

        var userId = "user_${UUID.randomUUID().toString().take(8)}"
        var token: String? = null

        // If a real Supabase endpoint is configured, try Supabase Auth REST API
        if (isSupabaseConfigured) {
            try {
                val signupUrl = "${supabaseUrl.trimEnd('/')}/auth/v1/signup"
                val jsonPayload = JSONObject().apply {
                    put("email", trimmedEmail)
                    put("password", password)
                }

                val request = Request.Builder()
                    .url(signupUrl)
                    .addHeader("apikey", supabaseAnonKey)
                    .addHeader("Content-Type", "application/json")
                    .post(jsonPayload.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val respJson = JSONObject(responseBody)
                    if (respJson.has("user")) {
                        val userObj = respJson.getJSONObject("user")
                        userId = userObj.optString("id", userId)
                    }
                    token = respJson.optString("access_token").takeIf { it.isNotEmpty() }
                } else {
                    val errorMsg = try {
                        JSONObject(responseBody).optString("msg", "Supabase error (${response.code})")
                    } catch (_: Exception) {
                        "Registration failed (${response.code})"
                    }
                    Log.w(TAG, "Supabase signup failed: $errorMsg - falling back to secure local session")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Supabase request failed: ${e.message} - falling back to secure local session")
            }
        }

        // Save account locally with password hash
        saveAccountCredentials(trimmedEmail, password)

        // Save session in state: NeedsProfileSetup
        val prefs = sharedPrefs
        prefs?.edit()
            ?.putBoolean(KEY_IS_LOGGED_IN, true)
            ?.putBoolean(KEY_NEEDS_PROFILE_SETUP, true)
            ?.putString(KEY_USER_ID, userId)
            ?.putString(KEY_USER_EMAIL, trimmedEmail)
            ?.putString(KEY_AUTH_TOKEN, token)
            ?.apply()

        val temporaryUser = User(
            id = userId,
            username = trimmedEmail.substringBefore("@"),
            fullName = "",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80",
            email = trimmedEmail
        )

        _currentUser.value = temporaryUser
        _authState.value = AuthState.NeedsProfileSetup(trimmedEmail, userId, token)

        return@withContext AuthResult.Success(temporaryUser, needsProfileSetup = true)
    }

    suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return@withContext AuthResult.Error("Please enter a valid email address.")
        }
        if (password.length < 6) {
            return@withContext AuthResult.Error("Password must be at least 6 characters.")
        }

        // Quick demo login check
        val isDemoUser = trimmedEmail.equals("alex@vibe.app", ignoreCase = true) ||
                         trimmedEmail.equals("demo@vibe.app", ignoreCase = true)

        var userId = "user_me"
        var token: String? = null
        var fullName = "Alex Rivera"
        var username = "alex_vibe"
        var avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80"
        var bio = "Architectural photographer & visual designer 📐✨\nExploring Tokyo, SF & NYC"
        var isVerified = true
        var needsProfileSetup = false

        if (isDemoUser) {
            // Instant demo login success
        } else if (isSupabaseConfigured) {
            // Attempt Supabase Password Grant Auth
            try {
                val tokenUrl = "${supabaseUrl.trimEnd('/')}/auth/v1/token?grant_type=password"
                val jsonPayload = JSONObject().apply {
                    put("email", trimmedEmail)
                    put("password", password)
                }

                val request = Request.Builder()
                    .url(tokenUrl)
                    .addHeader("apikey", supabaseAnonKey)
                    .addHeader("Content-Type", "application/json")
                    .post(jsonPayload.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val respJson = JSONObject(responseBody)
                    token = respJson.optString("access_token").takeIf { it.isNotEmpty() }
                    if (respJson.has("user")) {
                        val userObj = respJson.getJSONObject("user")
                        userId = userObj.optString("id", userId)
                        val meta = userObj.optJSONObject("user_metadata")
                        fullName = meta?.optString("full_name", fullName) ?: fullName
                        username = meta?.optString("username", trimmedEmail.substringBefore("@")) ?: trimmedEmail.substringBefore("@")
                        avatarUrl = meta?.optString("avatar_url", avatarUrl) ?: avatarUrl
                        bio = meta?.optString("bio", "") ?: ""
                    }
                } else {
                    // Check local credentials
                    val isLocalValid = verifyLocalCredentials(trimmedEmail, password)
                    if (!isLocalValid) {
                        return@withContext AuthResult.Error("Invalid email or password.")
                    }
                    userId = sharedPrefs?.getString(KEY_USER_ID, userId) ?: userId
                    fullName = sharedPrefs?.getString(KEY_FULL_NAME, fullName) ?: fullName
                    username = sharedPrefs?.getString(KEY_USERNAME, username) ?: username
                    avatarUrl = sharedPrefs?.getString(KEY_AVATAR_URL, avatarUrl) ?: avatarUrl
                    bio = sharedPrefs?.getString(KEY_BIO, bio) ?: bio
                    needsProfileSetup = sharedPrefs?.getBoolean(KEY_NEEDS_PROFILE_SETUP, false) ?: false
                }
            } catch (e: Exception) {
                Log.w(TAG, "Supabase connection error: ${e.message}, checking local session")
                val isLocalValid = verifyLocalCredentials(trimmedEmail, password)
                if (!isLocalValid) {
                    return@withContext AuthResult.Error("Invalid email or password.")
                }
            }
        } else {
            // In local/offline mode, verify local credentials or allow demo
            val isLocalValid = verifyLocalCredentials(trimmedEmail, password)
            if (!isLocalValid) {
                // If this is a first-time local login for any user, allow initial login
                saveAccountCredentials(trimmedEmail, password)
            }
            val storedName = sharedPrefs?.getString(KEY_FULL_NAME, "") ?: ""
            if (storedName.isBlank()) {
                needsProfileSetup = true
                username = trimmedEmail.substringBefore("@")
                fullName = ""
            } else {
                fullName = storedName
                username = sharedPrefs?.getString(KEY_USERNAME, trimmedEmail.substringBefore("@")) ?: trimmedEmail.substringBefore("@")
                avatarUrl = sharedPrefs?.getString(KEY_AVATAR_URL, avatarUrl) ?: avatarUrl
                bio = sharedPrefs?.getString(KEY_BIO, "") ?: ""
            }
        }

        val user = User(
            id = userId,
            username = username,
            fullName = fullName,
            avatarUrl = avatarUrl,
            isVerified = isVerified,
            bio = bio,
            email = trimmedEmail
        )

        // Save session
        sharedPrefs?.edit()
            ?.putBoolean(KEY_IS_LOGGED_IN, true)
            ?.putBoolean(KEY_NEEDS_PROFILE_SETUP, needsProfileSetup)
            ?.putString(KEY_USER_ID, userId)
            ?.putString(KEY_USER_EMAIL, trimmedEmail)
            ?.putString(KEY_USERNAME, username)
            ?.putString(KEY_FULL_NAME, fullName)
            ?.putString(KEY_AVATAR_URL, avatarUrl)
            ?.putString(KEY_BIO, bio)
            ?.putBoolean(KEY_IS_VERIFIED, isVerified)
            ?.putString(KEY_AUTH_TOKEN, token)
            ?.apply()

        _currentUser.value = user
        MockDataProvider.currentUser = user

        if (needsProfileSetup) {
            _authState.value = AuthState.NeedsProfileSetup(trimmedEmail, userId, token)
            return@withContext AuthResult.Success(user, needsProfileSetup = true)
        } else {
            _authState.value = AuthState.Authenticated(user, token)
            return@withContext AuthResult.Success(user, needsProfileSetup = false)
        }
    }

    suspend fun completeProfileSetup(
        fullName: String,
        username: String,
        bio: String,
        avatarUrl: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val trimmedName = fullName.trim()
        val trimmedHandle = username.trim().lowercase().removePrefix("@")

        if (trimmedName.isEmpty()) {
            return@withContext AuthResult.Error("Please enter your name.")
        }
        if (trimmedHandle.length < 3) {
            return@withContext AuthResult.Error("Username must be at least 3 characters.")
        }

        val token = sharedPrefs?.getString(KEY_AUTH_TOKEN, null)
        val userId = sharedPrefs?.getString(KEY_USER_ID, "user_${UUID.randomUUID().toString().take(8)}") ?: "user_me"
        val email = sharedPrefs?.getString(KEY_USER_EMAIL, "") ?: ""

        // If Supabase is configured and we have token, update user metadata in Supabase
        if (isSupabaseConfigured && token != null) {
            try {
                val updateUrl = "${supabaseUrl.trimEnd('/')}/auth/v1/user"
                val jsonPayload = JSONObject().apply {
                    put("data", JSONObject().apply {
                        put("full_name", trimmedName)
                        put("username", trimmedHandle)
                        put("avatar_url", avatarUrl)
                        put("bio", bio)
                    })
                }

                val request = Request.Builder()
                    .url(updateUrl)
                    .addHeader("apikey", supabaseAnonKey)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .put(jsonPayload.toString().toRequestBody(jsonMediaType))
                    .build()

                httpClient.newCall(request).execute()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to update Supabase profile metadata: ${e.message}")
            }
        }

        val finalUser = User(
            id = userId,
            username = trimmedHandle,
            fullName = trimmedName,
            avatarUrl = avatarUrl,
            isVerified = false,
            bio = bio,
            email = email
        )

        sharedPrefs?.edit()
            ?.putBoolean(KEY_IS_LOGGED_IN, true)
            ?.putBoolean(KEY_NEEDS_PROFILE_SETUP, false)
            ?.putString(KEY_USER_ID, userId)
            ?.putString(KEY_USERNAME, trimmedHandle)
            ?.putString(KEY_FULL_NAME, trimmedName)
            ?.putString(KEY_AVATAR_URL, avatarUrl)
            ?.putString(KEY_BIO, bio)
            ?.putBoolean(KEY_IS_VERIFIED, false)
            ?.apply()

        _currentUser.value = finalUser
        MockDataProvider.currentUser = finalUser
        _authState.value = AuthState.Authenticated(finalUser, token)

        return@withContext AuthResult.Success(finalUser, needsProfileSetup = false)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        val token = sharedPrefs?.getString(KEY_AUTH_TOKEN, null)
        if (isSupabaseConfigured && token != null) {
            try {
                val logoutUrl = "${supabaseUrl.trimEnd('/')}/auth/v1/logout"
                val request = Request.Builder()
                    .url(logoutUrl)
                    .addHeader("apikey", supabaseAnonKey)
                    .addHeader("Authorization", "Bearer $token")
                    .post("{}".toRequestBody(jsonMediaType))
                    .build()

                httpClient.newCall(request).execute()
            } catch (e: Exception) {
                Log.w(TAG, "Supabase logout request failed: ${e.message}")
            }
        }

        // Clear session preferences
        sharedPrefs?.edit()
            ?.remove(KEY_IS_LOGGED_IN)
            ?.remove(KEY_NEEDS_PROFILE_SETUP)
            ?.remove(KEY_USER_ID)
            ?.remove(KEY_AUTH_TOKEN)
            ?.remove(KEY_USER_EMAIL)
            ?.apply()

        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }

    private fun saveAccountCredentials(email: String, password: String) {
        val hash = hashPassword(password)
        sharedPrefs?.edit()
            ?.putString("cred_${email.lowercase()}", hash)
            ?.apply()
    }

    private fun verifyLocalCredentials(email: String, password: String): Boolean {
        val storedHash = sharedPrefs?.getString("cred_${email.lowercase()}", null) ?: return true
        return storedHash == hashPassword(password)
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val TAG = "AuthRepository"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_NEEDS_PROFILE_SETUP = "needs_profile_setup"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USERNAME = "user_username"
        private const val KEY_FULL_NAME = "user_full_name"
        private const val KEY_AVATAR_URL = "user_avatar_url"
        private const val KEY_BIO = "user_bio"
        private const val KEY_IS_VERIFIED = "user_is_verified"
        private const val KEY_AUTH_TOKEN = "auth_token"

        val instance: AuthRepository by lazy { AuthRepository() }
    }
}
