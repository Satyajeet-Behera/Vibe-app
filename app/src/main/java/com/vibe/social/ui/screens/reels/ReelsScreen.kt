package com.vibe.social.ui.screens.reels

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vibe.social.data.VibeRepository
import com.vibe.social.data.VibeRepositoryImpl
import com.vibe.social.model.Reel
import com.vibe.social.theme.VibeAccentBlue
import com.vibe.social.theme.VibeBlack
import com.vibe.social.theme.VibeLikeRed
import com.vibe.social.ui.components.DoubleTapHeartOverlay
import com.vibe.social.ui.components.ReelsVideoPlayer
import com.vibe.social.ui.screens.comments.CommentBottomSheet
import kotlinx.coroutines.launch

@Composable
fun ReelsScreen(
    onNavigateToCreate: () -> Unit = {},
    modifier: Modifier = Modifier,
    repository: VibeRepository = VibeRepositoryImpl.instance
) {
    val reels by repository.reels.collectAsState()
    val pagerState = rememberPagerState(pageCount = { reels.size })
    var activeCommentReelId by remember { mutableStateOf<String?>(null) }
    var isGlobalMuted by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VibeBlack)
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { reels[it].id }
        ) { page ->
            val reel = reels[page]
            val isCurrentPage = pagerState.currentPage == page

            ReelItemView(
                reel = reel,
                isCurrentPage = isCurrentPage,
                isMuted = isGlobalMuted,
                onToggleMute = { isGlobalMuted = !isGlobalMuted },
                onLikeClick = { repository.toggleReelLike(reel.id) },
                onFollowClick = { repository.toggleReelFollow(reel.id) },
                onCommentClick = { activeCommentReelId = reel.id },
                onShareClick = {
                    coroutineScope.launch {
                        Toast.makeText(context, "Reel link copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                },
                onDoubleTap = {
                    if (!reel.isLiked) {
                        repository.toggleReelLike(reel.id)
                    }
                }
            )
        }

        // Top Reels Header (Title & Actions)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reels",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { isGlobalMuted = !isGlobalMuted }) {
                    Icon(
                        imageVector = if (isGlobalMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isGlobalMuted) "Unmute" else "Mute",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = onNavigateToCreate) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = "Create Reel",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 56.dp)
        )

        // Active Comments Modal Sheet for Reel
        activeCommentReelId?.let { reelId ->
            CommentBottomSheet(
                postId = reelId,
                onDismiss = { activeCommentReelId = null }
            )
        }
    }
}

@Composable
fun ReelItemView(
    reel: Reel,
    isCurrentPage: Boolean,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onLikeClick: () -> Unit,
    onFollowClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onDoubleTap: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    var doubleTapTrigger by remember { mutableLongStateOf(0L) }
    var playbackProgress by remember { mutableFloatStateOf(0f) }
    var isPlaying by remember { mutableStateOf(true) }

    // Vinyl record rotation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_record_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(reel.id) {
                detectTapGestures(
                    onDoubleTap = {
                        doubleTapTrigger = System.currentTimeMillis()
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDoubleTap()
                    },
                    onTap = {
                        isPlaying = !isPlaying
                    }
                )
            }
    ) {
        // Real Video Playback or fallback thumbnail
        ReelsVideoPlayer(
            videoUrl = reel.videoUrl,
            thumbnailUrl = reel.videoThumbnailUrl,
            isCurrentPage = isCurrentPage,
            isPlaying = isPlaying,
            isMuted = isMuted,
            modifier = Modifier.fillMaxSize(),
            onProgressUpdate = { progress ->
                playbackProgress = progress
            }
        )

        // Gradient shadow at bottom for text readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Right Vertical Interaction Bar (Avatar, Like, Comment, Share, Audio)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Author Avatar with Follow Badge
            Box(
                modifier = Modifier.size(50.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = reel.author.avatarUrl,
                    contentDescription = reel.author.username,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                )

                // Follow badge (+) or (check)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (reel.isFollowing) VibeAccentBlue else VibeLikeRed)
                        .border(1.dp, Color.White, CircleShape)
                        .clickable(onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onFollowClick()
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (reel.isFollowing) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = "Follow",
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            // Like Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onLikeClick()
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (reel.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (reel.isLiked) "Unlike" else "Like",
                        tint = if (reel.isLiked) VibeLikeRed else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "${reel.likeCount / 1000}k",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Comment Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onCommentClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = "${reel.commentCount}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Share Action
            IconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onShareClick()
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Rotating Vinyl Record Album Cover
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    .clickable { onToggleMute() }
                    .rotate(if (isPlaying) rotationAngle else 0f),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = reel.author.avatarUrl,
                    contentDescription = "Audio track cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                )
            }
        }

        // Bottom Content Overlay (@username, caption, audio marquee)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.76f)
                .padding(start = 16.dp, bottom = 82.dp)
        ) {
            // Username & Follow
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "@${reel.author.username}",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (reel.isFollowing) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                        .clickable(onClick = onFollowClick)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (reel.isFollowing) "Following" else "Follow",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Caption
            Text(
                text = reel.caption,
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Audio Track Marquee Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .clickable { onToggleMute() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = reel.audioTrackName,
                    color = Color.White,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Bouncy glowing heart animation overlay on double tap
        DoubleTapHeartOverlay(
            trigger = doubleTapTrigger,
            modifier = Modifier.fillMaxSize()
        )

        // Video playback progress bar at the very bottom
        LinearProgressIndicator(
            progress = { playbackProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.5.dp)
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.25f)
        )
    }
}
