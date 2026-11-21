package com.vibe.social.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

@Composable
fun PinchZoomMedia(
    modifier: Modifier = Modifier,
    onDoubleTap: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1f) }
    val offsetXAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(0f) }

    var isZooming by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .zIndex(if (isZooming) 10f else 1f)
            .graphicsLayer {
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
                translationX = offsetXAnim.value
                translationY = offsetYAnim.value
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onDoubleTap()
                    }
                )
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val canceled = event.changes.any { it.isConsumed }
                        if (!canceled && event.changes.size > 1) {
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()

                            coroutineScope.launch {
                                val newScale = (scaleAnim.value * zoomChange).coerceIn(1f, 4f)
                                scaleAnim.snapTo(newScale)
                                isZooming = newScale > 1.05f

                                if (isZooming) {
                                    offsetXAnim.snapTo(offsetXAnim.value + panChange.x)
                                    offsetYAnim.snapTo(offsetYAnim.value + panChange.y)
                                }
                            }

                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })

                    // Snaps back smoothly to 1.0f on release
                    isZooming = false
                    coroutineScope.launch {
                        scaleAnim.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                    coroutineScope.launch {
                        offsetXAnim.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                    coroutineScope.launch {
                        offsetYAnim.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                }
            }
    ) {
        content()
    }
}
