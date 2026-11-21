package com.vibe.social.ui.components

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun ReelsVideoPlayer(
    videoUrl: String,
    thumbnailUrl: String,
    isCurrentPage: Boolean,
    isPlaying: Boolean,
    isMuted: Boolean,
    modifier: Modifier = Modifier,
    onProgressUpdate: (Float) -> Unit = {}
) {
    var isBuffering by remember { mutableStateOf(true) }
    var isVideoReady by remember { mutableStateOf(false) }
    var playerRef by remember { mutableStateOf<MediaPlayer?>(null) }

    // Progress polling when active and playing
    LaunchedEffect(isCurrentPage, isPlaying, isVideoReady) {
        if (isCurrentPage && isPlaying && isVideoReady) {
            while (isActive) {
                playerRef?.let { mp ->
                    try {
                        if (mp.isPlaying && mp.duration > 0) {
                            val fraction = mp.currentPosition.toFloat() / mp.duration.toFloat()
                            onProgressUpdate(fraction.coerceIn(0f, 1f))
                        }
                    } catch (_: Exception) {}
                }
                delay(100)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        // 1. Fallback / Loading Thumbnail Image
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = "Video thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Hardware-accelerated Video TextureView
        if (isCurrentPage && videoUrl.isNotBlank()) {
            AndroidView(
                factory = { ctx ->
                    TextureView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                                val surface = Surface(surfaceTexture)
                                val mp = MediaPlayer().apply {
                                    setAudioAttributes(
                                        AudioAttributes.Builder()
                                            .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                                            .setUsage(AudioAttributes.USAGE_MEDIA)
                                            .build()
                                    )
                                    setSurface(surface)
                                    isLooping = true
                                    val vol = if (isMuted) 0f else 1f
                                    setVolume(vol, vol)

                                    setOnPreparedListener { preparedMp ->
                                        isBuffering = false
                                        isVideoReady = true
                                        // Adjust CenterCrop matrix
                                        val videoWidth = preparedMp.videoWidth.toFloat()
                                        val videoHeight = preparedMp.videoHeight.toFloat()
                                        if (videoWidth > 0 && videoHeight > 0 && width > 0 && height > 0) {
                                            val scaleX = width / videoWidth
                                            val scaleY = height / videoHeight
                                            val maxScale = maxOf(scaleX, scaleY)

                                            val scaledWidth = videoWidth * maxScale
                                            val scaledHeight = videoHeight * maxScale
                                            val dx = (width - scaledWidth) / 2f
                                            val dy = (height - scaledHeight) / 2f

                                            val matrix = Matrix().apply {
                                                setScale(scaledWidth / width, scaledHeight / height)
                                                postTranslate(dx, dy)
                                            }
                                            setTransform(matrix)
                                        }

                                        if (isPlaying) {
                                            preparedMp.start()
                                        }
                                    }

                                    setOnErrorListener { _, _, _ ->
                                        isBuffering = false
                                        true
                                    }

                                    try {
                                        setDataSource(videoUrl)
                                        prepareAsync()
                                        isBuffering = true
                                    } catch (e: Exception) {
                                        isBuffering = false
                                    }
                                }
                                playerRef = mp
                            }

                            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

                            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                                playerRef?.let {
                                    try {
                                        if (it.isPlaying) it.stop()
                                        it.reset()
                                        it.release()
                                    } catch (_: Exception) {}
                                }
                                playerRef = null
                                isVideoReady = false
                                return true
                            }

                            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                        }
                    }
                },
                update = { textureView ->
                    playerRef?.let { mp ->
                        try {
                            val vol = if (isMuted) 0f else 1f
                            mp.setVolume(vol, vol)

                            if (isPlaying && !mp.isPlaying && isVideoReady) {
                                mp.start()
                            } else if (!isPlaying && mp.isPlaying) {
                                mp.pause()
                            }
                        } catch (_: Exception) {}
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 3. Buffering Indicator
        AnimatedVisibility(
            visible = isCurrentPage && isBuffering,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
            }
        }

        // 4. Paused Icon indicator
        AnimatedVisibility(
            visible = isCurrentPage && !isPlaying && isVideoReady,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Paused",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }

    DisposableEffect(videoUrl) {
        onDispose {
            playerRef?.let {
                try {
                    if (it.isPlaying) it.stop()
                    it.reset()
                    it.release()
                } catch (_: Exception) {}
            }
            playerRef = null
            isVideoReady = false
        }
    }
}
