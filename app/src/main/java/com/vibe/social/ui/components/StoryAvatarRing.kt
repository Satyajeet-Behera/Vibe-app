package com.vibe.social.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vibe.social.theme.InstagramStoryGradientBrush
import com.vibe.social.theme.InstagramStoryViewedBrush
import com.vibe.social.theme.VibeAccentBlue
import com.vibe.social.theme.VibeBlack

@Composable
fun StoryAvatarRing(
    avatarUrl: String,
    modifier: Modifier = Modifier,
    size: Dp = 68.dp,
    borderWidth: Dp = 2.5.dp,
    isViewed: Boolean = false,
    hasStory: Boolean = true,
    isCurrentUser: Boolean = false,
    showAddBadge: Boolean = false,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "story_avatar_scale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(animatedScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        val outerModifier = if (hasStory) {
            if (isViewed) {
                Modifier
                    .size(size)
                    .border(width = borderWidth, color = Color(0xFF3F3F46).copy(alpha = 0.6f), shape = CircleShape)
                    .padding(borderWidth + 2.dp)
            } else {
                Modifier
                    .size(size)
                    .border(width = borderWidth, brush = InstagramStoryGradientBrush, shape = CircleShape)
                    .padding(borderWidth + 2.dp) // Gap between ring and avatar
            }
        } else {
            Modifier
                .size(size)
                .padding(2.dp)
        }

        // Inner Avatar Image
        AsyncImage(
            model = avatarUrl,
            contentDescription = "User Avatar",
            contentScale = ContentScale.Crop,
            modifier = outerModifier
                .clip(CircleShape)
                .background(Color(0xFF121212))
        )

        // Add story badge (+) for current user
        if (showAddBadge) {
            Box(
                modifier = Modifier
                    .size((size.value * 0.32f).dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(VibeAccentBlue)
                    .border(2.dp, Color(0xFF121212), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Story",
                    tint = Color.White,
                    modifier = Modifier.size((size.value * 0.20f).dp)
                )
            }
        }
    }
}
