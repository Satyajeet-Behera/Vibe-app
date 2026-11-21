package com.vibe.social.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vibe.social.data.auth.AuthRepository
import com.vibe.social.data.auth.AuthResult
import com.vibe.social.theme.InstagramStoryGradientBrush
import com.vibe.social.theme.VibeAccentBlue
import com.vibe.social.theme.VibeBlack
import com.vibe.social.theme.VibeBorder
import com.vibe.social.theme.VibeDarkCard
import com.vibe.social.theme.VibeZinc400
import com.vibe.social.theme.VibeZinc500
import com.vibe.social.theme.VibeZinc800
import com.vibe.social.theme.VibeZinc900
import kotlinx.coroutines.launch

private val PRESET_AVATARS = listOf(
    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=500&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=500&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500&auto=format&fit=crop&q=80"
)

@Composable
fun ProfileSetupScreen(
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier,
    authRepo: AuthRepository = AuthRepository.instance
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val initialEmail = (authRepo.authState.value as? com.vibe.social.data.auth.AuthState.NeedsProfileSetup)?.email ?: ""
    val defaultUsername = initialEmail.substringBefore("@").ifBlank { "creator_vibe" }

    var selectedAvatar by remember { mutableStateOf(PRESET_AVATARS[0]) }
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf(defaultUsername) }
    var bio by remember { mutableStateOf("Digital creator & visual storyteller ✨") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun submitProfile() {
        if (isLoading) return
        errorMessage = null

        if (fullName.trim().isEmpty()) {
            errorMessage = "Please enter your full name."
            return
        }
        if (username.trim().length < 3) {
            errorMessage = "Username must be at least 3 characters."
            return
        }

        isLoading = true
        focusManager.clearFocus()

        coroutineScope.launch {
            val result = authRepo.completeProfileSetup(
                fullName = fullName,
                username = username,
                bio = bio,
                avatarUrl = selectedAvatar
            )
            isLoading = false
            when (result) {
                is AuthResult.Success -> {
                    onSetupComplete()
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
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Set Up Your Profile",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Customize how you appear to others on Vibe",
                fontSize = 13.sp,
                color = VibeZinc400,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Prominent Avatar with Story Ring
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape)
                        .background(InstagramStoryGradientBrush)
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(VibeBlack)
                        .padding(3.dp)
                        .clip(CircleShape)
                ) {
                    AsyncImage(
                        model = selectedAvatar,
                        contentDescription = "Selected avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(VibeAccentBlue)
                        .border(2.dp, VibeBlack, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Change photo",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Preset Avatars Carousel
            Text(
                text = "CHOOSE AVATAR PRESET",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = VibeZinc500,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PRESET_AVATARS.forEachIndexed { index, avatarUrl ->
                    val isSelected = selectedAvatar == avatarUrl
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) VibeAccentBlue else VibeBorder,
                                shape = CircleShape
                            )
                            .clickable { selectedAvatar = avatarUrl }
                    ) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Avatar $index",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Error Banner
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

            // Full Name Input
            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    if (errorMessage != null) errorMessage = null
                },
                placeholder = { Text("Your full name (e.g. Alex Rivera)", color = VibeZinc500, fontSize = 14.sp) },
                label = { Text("Full Name", color = VibeZinc400, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = VibeZinc500,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
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
                    .testTag("setup_name_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Username Input
            OutlinedTextField(
                value = username,
                onValueChange = { input ->
                    username = input.filter { it.isLetterOrDigit() || it == '_' }.lowercase()
                    if (errorMessage != null) errorMessage = null
                },
                placeholder = { Text("username", color = VibeZinc500, fontSize = 14.sp) },
                label = { Text("Username Handle", color = VibeZinc400, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.AlternateEmail,
                        contentDescription = null,
                        tint = VibeZinc500,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
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
                    .testTag("setup_username_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Bio Input
            OutlinedTextField(
                value = bio,
                onValueChange = {
                    if (it.length <= 150) {
                        bio = it
                        if (errorMessage != null) errorMessage = null
                    }
                },
                placeholder = { Text("Tell the community about yourself...", color = VibeZinc500, fontSize = 14.sp) },
                label = { Text("Bio", color = VibeZinc400, fontSize = 12.sp) },
                maxLines = 4,
                minLines = 3,
                supportingText = {
                    Text(
                        text = "${bio.length}/150",
                        color = VibeZinc500,
                        fontSize = 11.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
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
                    .testTag("setup_bio_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Complete Setup Button
            Button(
                onClick = { submitProfile() },
                enabled = !isLoading && fullName.isNotBlank() && username.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VibeAccentBlue,
                    disabledContainerColor = VibeAccentBlue.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("complete_setup_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Enter Vibe",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
