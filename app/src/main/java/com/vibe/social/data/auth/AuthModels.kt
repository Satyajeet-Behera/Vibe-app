package com.vibe.social.data.auth

import androidx.compose.runtime.Immutable
import com.vibe.social.model.User

sealed interface AuthState {
    object Unauthenticated : AuthState
    object Loading : AuthState
    data class Authenticated(val user: User, val token: String? = null) : AuthState
    data class NeedsProfileSetup(val email: String, val userId: String, val token: String? = null) : AuthState
}

sealed interface AuthResult {
    data class Success(val user: User, val needsProfileSetup: Boolean = false) : AuthResult
    data class Error(val message: String) : AuthResult
}

@Immutable
data class SupabaseConfig(
    val url: String,
    val anonKey: String,
    val isCustomConfigured: Boolean
)
