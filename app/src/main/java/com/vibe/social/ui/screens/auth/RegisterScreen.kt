package com.vibe.social.ui.screens.auth

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibe.social.data.auth.AuthRepository
import com.vibe.social.data.auth.AuthResult
import com.vibe.social.theme.VibeAccentBlue
import com.vibe.social.theme.VibeBlack
import com.vibe.social.theme.VibeDarkCard
import com.vibe.social.theme.VibeZinc400
import com.vibe.social.theme.VibeZinc500
import com.vibe.social.theme.VibeZinc800
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    authRepo: AuthRepository = AuthRepository.instance
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isEmailValid by remember {
        derivedStateOf { email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() }
    }
    val isPasswordLengthValid by remember {
        derivedStateOf { password.length >= 6 }
    }
    val doPasswordsMatch by remember {
        derivedStateOf { password.isNotEmpty() && password == confirmPassword }
    }

    fun performRegister() {
        if (isLoading) return
        errorMessage = null

        if (!isEmailValid) {
            errorMessage = "Please enter a valid email address."
            return
        }
        if (!isPasswordLengthValid) {
            errorMessage = "Password must be at least 6 characters."
            return
        }
        if (!doPasswordsMatch) {
            errorMessage = "Passwords do not match."
            return
        }

        isLoading = true
        focusManager.clearFocus()

        coroutineScope.launch {
            val result = authRepo.register(email, password)
            isLoading = false
            when (result) {
                is AuthResult.Success -> {
                    onRegistrationSuccess()
                }
                is AuthResult.Error -> {
                    errorMessage = result.message
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VibeBlack)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_vibe_logo),
                    contentDescription = "Vibe App Logo",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Vibe",
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Create your account to start sharing",
                    fontSize = 13.sp,
                    color = VibeZinc500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
                )
            }

            // Error banner
            if (errorMessage != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Error",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 12.sp,
                        color = Color(0xFFFCA5A5),
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (errorMessage != null) errorMessage = null
                },
                placeholder = { Text("Email address", color = VibeZinc500, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = if (isEmailValid) VibeAccentBlue else VibeZinc500,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (isEmailValid) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Valid email",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VibeAccentBlue,
                    unfocusedBorderColor = VibeZinc800,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = VibeDarkCard,
                    unfocusedContainerColor = VibeDarkCard
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_email_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (errorMessage != null) errorMessage = null
                },
                placeholder = { Text("Password (min 6 characters)", color = VibeZinc500, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = if (isPasswordLengthValid) VibeAccentBlue else VibeZinc500,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = VibeZinc500,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VibeAccentBlue,
                    unfocusedBorderColor = VibeZinc800,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = VibeDarkCard,
                    unfocusedContainerColor = VibeDarkCard
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_password_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm Password Input
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (errorMessage != null) errorMessage = null
                },
                placeholder = { Text("Confirm password", color = VibeZinc500, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = if (doPasswordsMatch) VibeAccentBlue else VibeZinc500,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                            tint = VibeZinc500,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { performRegister() }
                ),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VibeAccentBlue,
                    unfocusedBorderColor = VibeZinc800,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = VibeDarkCard,
                    unfocusedContainerColor = VibeDarkCard
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_confirm_password_input")
            )

            // Password helper rules
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = if (isPasswordLengthValid) Color(0xFF10B981) else VibeZinc500,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "At least 6 characters",
                    fontSize = 11.sp,
                    color = if (isPasswordLengthValid) Color(0xFF10B981) else VibeZinc500
                )
                if (confirmPassword.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = if (doPasswordsMatch) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (doPasswordsMatch) "Passwords match" else "Passwords differ",
                        fontSize = 11.sp,
                        color = if (doPasswordsMatch) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Create Account Button
            Button(
                onClick = { performRegister() },
                enabled = !isLoading && isEmailValid && isPasswordLengthValid && doPasswordsMatch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VibeAccentBlue,
                    disabledContainerColor = VibeAccentBlue.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("register_submit_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Continue to Profile Setup",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Already have account
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Already have an account?",
                    fontSize = 13.sp,
                    color = VibeZinc400
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sign In",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = VibeAccentBlue,
                    modifier = Modifier
                        .clickable { onNavigateToLogin() }
                        .padding(4.dp)
                        .testTag("login_link")
                )
            }
        }
    }
}
