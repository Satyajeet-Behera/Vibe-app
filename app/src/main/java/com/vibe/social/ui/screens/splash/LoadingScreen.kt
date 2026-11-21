package com.vibe.social.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Loading interface of the application displayed whenever the user opens the app.
 * Features the signature glowing neon ribbon wave and luminous "VIBE" typography.
 */
@Composable
fun LoadingScreen(
    onLoadingFinished: () -> Unit,
    modifier: Modifier = Modifier,
    durationMillis: Long = 2200L
) {
    // Progress animation for the loading bar
    val progress = remember { Animatable(0f) }
    // Entrance fade & scale animation
    val contentAlpha = remember { Animatable(0f) }
    val contentScale = remember { Animatable(0.92f) }

    // Pulsing neon breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "NeonBreath")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse"
    )

    LaunchedEffect(Unit) {
        // Smooth entrance
        contentAlpha.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
        contentScale.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
    }

    LaunchedEffect(Unit) {
        // Progress bar smooth fill
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis.toInt() - 200, easing = FastOutSlowInEasing)
        )
        delay(200L)
        onLoadingFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF040406))
            .testTag("app_loading_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background glow: Radial gradients matching the neon colors
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width * 0.5f, size.height * 0.46f)
            // Center purple/magenta ambient bloom
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x33EC4899),
                        Color(0x188B5CF6),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = size.width * 0.55f
                ),
                radius = size.width * 0.55f,
                center = centerOffset
            )
            // Left blue ambient bloom
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x2800D4FF), Color.Transparent),
                    center = Offset(size.width * 0.35f, size.height * 0.44f),
                    radius = size.width * 0.4f
                ),
                radius = size.width * 0.4f,
                center = Offset(size.width * 0.35f, size.height * 0.44f)
            )
            // Right warm amber ambient bloom
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x25F97316), Color.Transparent),
                    center = Offset(size.width * 0.65f, size.height * 0.44f),
                    radius = size.width * 0.4f
                ),
                radius = size.width * 0.4f,
                center = Offset(size.width * 0.65f, size.height * 0.44f)
            )
        }

        // Central Branding: Glowing Neon Ribbon Wave + Luminous "VIBE" Typography
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(contentAlpha.value)
                .padding(horizontal = 24.dp)
        ) {
            // Neon Ribbon Wave
            Box(
                modifier = Modifier
                    .size(width = 320.dp, height = 180.dp),
                contentAlignment = Alignment.Center
            ) {
                GlowingNeonWave(
                    pulseScale = glowPulse,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Glowing "VIBE" Typography
            Text(
                text = "VIBE",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 52.sp,
                    letterSpacing = 10.sp,
                    color = Color.White,
                    shadow = Shadow(
                        color = Color(0xFFFFF1E6).copy(alpha = 0.9f),
                        blurRadius = 30f * glowPulse,
                        offset = Offset.Zero
                    )
                ),
                modifier = Modifier.padding(start = 10.sp.value.dp) // Compensate letter spacing for centering
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Luminous secondary glow aura behind text
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x6000D4FF),
                                Color(0x90EC4899),
                                Color(0x60F97316),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Bottom Loading Interface Status
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 36.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sleek Gradient Progress Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(Color(0xFF1E1E24))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF00D4FF),
                                    Color(0xFFEC4899),
                                    Color(0xFFF97316)
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Brand Pill (matching reference image)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x1AFFFFFF))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "VIBE SOCIAL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                    color = Color(0x99FFFFFF)
                )
            }
        }
    }
}

/**
 * Draws the signature intertwined neon ribbon wave from the reference image.
 * Uses multiple layered strokes with smooth gradient transitions and soft neon diffusion.
 */
@Composable
private fun GlowingNeonWave(
    pulseScale: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Define the two intertwined flowing ribbons
        // Ribbon 1: The primary outer looping wave (Left cyan -> Center magenta -> Right warm amber flourish)
        val path1 = Path().apply {
            moveTo(w * 0.14f, h * 0.32f)
            cubicTo(
                w * 0.20f, h * 0.44f,
                w * 0.24f, h * 0.68f,
                w * 0.36f, h * 0.68f
            )
            cubicTo(
                w * 0.48f, h * 0.68f,
                w * 0.48f, h * 0.34f,
                w * 0.58f, h * 0.34f
            )
            cubicTo(
                w * 0.68f, h * 0.34f,
                w * 0.68f, h * 0.64f,
                w * 0.78f, h * 0.64f
            )
            cubicTo(
                w * 0.86f, h * 0.64f,
                w * 0.88f, h * 0.44f,
                w * 0.94f, h * 0.24f
            )
        }

        // Ribbon 2: The intertwining inner wave creating the loop overlay
        val path2 = Path().apply {
            moveTo(w * 0.12f, h * 0.48f)
            cubicTo(
                w * 0.18f, h * 0.60f,
                w * 0.28f, h * 0.76f,
                w * 0.38f, h * 0.60f
            )
            cubicTo(
                w * 0.48f, h * 0.44f,
                w * 0.52f, h * 0.46f,
                w * 0.62f, h * 0.60f
            )
            cubicTo(
                w * 0.72f, h * 0.74f,
                w * 0.80f, h * 0.68f,
                w * 0.88f, h * 0.52f
            )
            cubicTo(
                w * 0.91f, h * 0.46f,
                w * 0.93f, h * 0.42f,
                w * 0.96f, h * 0.42f
            )
        }

        // Multi-stop neon gradients matching the exact color transition in the reference image
        val neonBrush1 = Brush.linearGradient(
            colors = listOf(
                Color(0xFF00E5FF), // Electric Cyan
                Color(0xFF2979FF), // Vivid Azure
                Color(0xFFD500F9), // Neon Purple
                Color(0xFFFF1493), // Hot Magenta
                Color(0xFFFF6D00), // Fiery Orange
                Color(0xFFFFD600)  // Golden Amber
            ),
            start = Offset(w * 0.12f, h * 0.4f),
            end = Offset(w * 0.96f, h * 0.3f)
        )

        val neonBrush2 = Brush.linearGradient(
            colors = listOf(
                Color(0xFF00B0FF), // Sky Blue
                Color(0xFF3D5AFE), // Deep Indigo
                Color(0xFFF50057), // Neon Crimson
                Color(0xFFFF4081), // Pink
                Color(0xFFFFAB00)  // Amber
            ),
            start = Offset(w * 0.12f, h * 0.5f),
            end = Offset(w * 0.96f, h * 0.5f)
        )

        // Pass 1: Wide Diffused Ambient Glow (Multiplied by pulseScale)
        val outerGlowWidth = 28.dp.toPx() * pulseScale
        drawPath(
            path = path1,
            brush = neonBrush1,
            style = Stroke(
                width = outerGlowWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
            alpha = 0.18f
        )
        drawPath(
            path = path2,
            brush = neonBrush2,
            style = Stroke(
                width = outerGlowWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
            alpha = 0.18f
        )

        // Pass 2: Medium Neon Bloom
        val mediumBloomWidth = 16.dp.toPx() * pulseScale
        drawPath(
            path = path1,
            brush = neonBrush1,
            style = Stroke(
                width = mediumBloomWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
            alpha = 0.45f
        )
        drawPath(
            path = path2,
            brush = neonBrush2,
            style = Stroke(
                width = mediumBloomWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
            alpha = 0.45f
        )

        // Pass 3: Solid Saturated Core Stroke
        val coreStrokeWidth = 7.5.dp.toPx()
        drawPath(
            path = path1,
            brush = neonBrush1,
            style = Stroke(
                width = coreStrokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
            alpha = 0.95f
        )
        drawPath(
            path = path2,
            brush = neonBrush2,
            style = Stroke(
                width = coreStrokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
            alpha = 0.95f
        )

        // Pass 4: Glass-tube High-Luminance Inner Spine (Pure white highlight)
        val innerSpineWidth = 2.2.dp.toPx()
        drawPath(
            path = path1,
            color = Color.White.copy(alpha = 0.85f),
            style = Stroke(
                width = innerSpineWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawPath(
            path = path2,
            color = Color.White.copy(alpha = 0.80f),
            style = Stroke(
                width = innerSpineWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
