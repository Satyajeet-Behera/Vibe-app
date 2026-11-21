package com.vibe.social.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.vibe.social.theme.VibeLikeRed

@Composable
fun DoubleTapHeartOverlay(
    trigger: Long,
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = {}
) {
    if (trigger <= 0L) return

    val scale = remember(trigger) { Animatable(0f) }
    val alpha = remember(trigger) { Animatable(1f) }

    LaunchedEffect(trigger) {
        scale.snapTo(0f)
        alpha.snapTo(1f)
        // Spring pop up to 1.3f
        scale.animateTo(
            targetValue = 1.3f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        // Gentle settle to 1.0f while fading out
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 150)
        )
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 250)
        )
        onAnimationEnd()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (alpha.value > 0f) {
            // Drop shadow / glow behind heart
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color.Black.copy(alpha = alpha.value * 0.4f),
                modifier = Modifier
                    .size(110.dp)
                    .graphicsLayer {
                        scaleX = scale.value * 1.08f
                        scaleY = scale.value * 1.08f
                        this.alpha = alpha.value * 0.5f
                    }
            )
            // Vibrant red heart
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Liked",
                tint = VibeLikeRed,
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        this.alpha = alpha.value
                    }
            )
        }
    }
}
