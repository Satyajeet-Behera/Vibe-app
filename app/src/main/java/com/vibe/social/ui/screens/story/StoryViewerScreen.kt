package com.vibe.social.ui.screens.story

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vibe.social.data.VibeRepository
import com.vibe.social.data.VibeRepositoryImpl
import com.vibe.social.theme.VibeAccentBlue
import com.vibe.social.theme.VibeBlack
import com.vibe.social.theme.VibeLikeRed
import com.vibe.social.ui.components.DoubleTapHeartOverlay
import kotlinx.coroutines.delay

@Composable
fun StoryViewerScreen(
    userId: String,
    onClose: () -> Unit,
    repository: VibeRepository = VibeRepositoryImpl.instance
) {
    val allStories by repository.stories.collectAsState()
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    // Distinct list of users who have active stories
    val usersWithStories = remember(allStories) {
        allStories.map { it.user.id }.distinct()
    }

    if (allStories.isEmpty() || usersWithStories.isEmpty()) {
        LaunchedEffect(Unit) { onClose() }
        return
    }

    // Determine initial user index based on clicked avatar
    val initialUserIndex = remember(userId, usersWithStories) {
        val found = usersWithStories.indexOf(userId)
        if (found >= 0) found else 0
    }

    var currentUserIndex by remember { mutableIntStateOf(initialUserIndex) }
    var currentStoryIndexInUser by remember { mutableIntStateOf(0) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var isLikedCurrentStory by remember { mutableStateOf(false) }
    var heartAnimTrigger by remember { mutableLongStateOf(0L) }

    val safeUserIndex = currentUserIndex.coerceIn(0, usersWithStories.size - 1)
    val currentUserId = usersWithStories[safeUserIndex]
    val activeUserStories = remember(allStories, currentUserId) {
        allStories.filter { it.user.id == currentUserId }
    }

    val safeStoryIndex = currentStoryIndexInUser.coerceIn(0, (activeUserStories.size - 1).coerceAtLeast(0))
    val currentStory = activeUserStories.getOrNull(safeStoryIndex) ?: allStories.first()

    // Reset like state per story
    LaunchedEffect(currentStory.id) {
        isLikedCurrentStory = false
        repository.viewStory(currentStory.id)
    }

    // Continuous playback loop: tick every 50ms, auto advance to next story and next user!
    LaunchedEffect(currentUserId, safeStoryIndex, isPaused) {
        if (!isPaused) {
            val totalDurationMs = 4500L
            val stepMs = 50L
            val stepFraction = stepMs.toFloat() / totalDurationMs

            while (progress < 1.0f) {
                delay(stepMs)
                progress += stepFraction
            }

            // Time is up -> advance seamlessly
            if (currentStoryIndexInUser < activeUserStories.size - 1) {
                // Next story for this user
                currentStoryIndexInUser++
                progress = 0f
            } else if (currentUserIndex < usersWithStories.size - 1) {
                // Next user's stories
                currentUserIndex++
                currentStoryIndexInUser = 0
                progress = 0f
            } else {
                // Loop continuously from the first user so playback never breaks!
                currentUserIndex = 0
                currentStoryIndexInUser = 0
                progress = 0f
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(VibeBlack)
    ) {
        // Background Story Image
        AsyncImage(
            model = currentStory.mediaUrl,
            contentDescription = "Story Media",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient at top and bottom for readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                    )
                )
        )

        // Gesture detection: Tap Left 30%, Tap Right 70%, Press and Hold to pause, Double Tap to like
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(currentStory.id) {
                    detectTapGestures(
                        onPress = {
                            isPaused = true
                            tryAwaitRelease()
                            isPaused = false
                        },
                        onDoubleTap = {
                            isLikedCurrentStory = true
                            heartAnimTrigger = System.currentTimeMillis()
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onTap = { offset ->
                            if (offset.x < size.width * 0.3f) {
                                // Previous story or previous user
                                if (currentStoryIndexInUser > 0) {
                                    currentStoryIndexInUser--
                                    progress = 0f
                                } else if (currentUserIndex > 0) {
                                    currentUserIndex--
                                    val prevStories = allStories.filter { it.user.id == usersWithStories[currentUserIndex] }
                                    currentStoryIndexInUser = (prevStories.size - 1).coerceAtLeast(0)
                                    progress = 0f
                                } else {
                                    progress = 0f
                                }
                            } else {
                                // Next story or next user
                                if (currentStoryIndexInUser < activeUserStories.size - 1) {
                                    currentStoryIndexInUser++
                                    progress = 0f
                                } else if (currentUserIndex < usersWithStories.size - 1) {
                                    currentUserIndex++
                                    currentStoryIndexInUser = 0
                                    progress = 0f
                                } else {
                                    // Finished all stories, return to home
                                    onClose()
                                }
                            }
                        }
                    )
                }
        )

        // UI Overlays (hidden while pressing and holding)
        AnimatedVisibility(
            visible = !isPaused,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Top Segmented Progress Indicators for current user's stories
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    activeUserStories.forEachIndexed { index, _ ->
                        val segmentProgress = when {
                            index < currentStoryIndexInUser -> 1.0f
                            index == currentStoryIndexInUser -> progress.coerceIn(0f, 1f)
                            else -> 0.0f
                        }
                        LinearProgressIndicator(
                            progress = { segmentProgress },
                            modifier = Modifier
                                .weight(1f)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }

                // User Info Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = currentStory.user.avatarUrl,
                            contentDescription = currentStory.user.username,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = currentStory.user.username,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = currentStory.timestamp,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Story",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Reply Bar & Like Action
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = {
                                Text("Send message...", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White.copy(alpha = 0.6f),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = VibeAccentBlue
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Like Story Button
                        IconButton(
                            onClick = {
                                isLikedCurrentStory = !isLikedCurrentStory
                                if (isLikedCurrentStory) {
                                    heartAnimTrigger = System.currentTimeMillis()
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isLikedCurrentStory) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Like Story",
                                tint = if (isLikedCurrentStory) VibeLikeRed else Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Send Message Button
                        IconButton(
                            onClick = {
                                if (replyText.isNotBlank()) {
                                    Toast.makeText(context, "Reply sent to ${currentStory.user.username}!", Toast.LENGTH_SHORT).show()
                                    replyText = ""
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Message",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bouncy heart popup on double tap or like button
        DoubleTapHeartOverlay(
            trigger = heartAnimTrigger,
            modifier = Modifier.fillMaxSize()
        )
    }
}
