package com.vibe.social.ui.screens.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vibe.social.data.MockDataProvider
import com.vibe.social.data.VibeRepository
import com.vibe.social.data.VibeRepositoryImpl
import com.vibe.social.model.Post
import com.vibe.social.theme.VibeAccentBlue
import com.vibe.social.theme.VibeBlack
import com.vibe.social.theme.VibeBorder
import com.vibe.social.theme.VibeBorderSubtle
import com.vibe.social.theme.VibeDarkCard
import com.vibe.social.theme.VibeDarkSurface
import com.vibe.social.theme.VibeLightBorder
import com.vibe.social.theme.VibeTextPrimary
import com.vibe.social.theme.VibeTextSecondary
import com.vibe.social.theme.VibeTextTertiary
import com.vibe.social.theme.VibeZinc400
import com.vibe.social.theme.VibeZinc500
import com.vibe.social.theme.VibeZinc900
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

enum class PostFilter(val label: String, val matrix: ColorMatrix) {
    Normal("Normal", ColorMatrix()),
    Noir("Noir", ColorMatrix().apply { setToSaturation(0f) }),
    Vintage("Vintage", ColorMatrix(floatArrayOf(
        1.2f, 0.1f, 0.1f, 0f, 20f,
        0.1f, 1.0f, 0.1f, 0f, 10f,
        0.0f, 0.1f, 0.8f, 0f, -10f,
        0f, 0f, 0f, 1f, 0f
    ))),
    Vibrant("Vibrant", ColorMatrix(floatArrayOf(
        1.3f, 0f, 0f, 0f, -15f,
        0f, 1.3f, 0f, 0f, -15f,
        0f, 0f, 1.3f, 0f, -15f,
        0f, 0f, 0f, 1f, 0f
    )))
}

@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    repository: VibeRepository = VibeRepositoryImpl.instance
) {
    val coroutineScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    // Sample selected image from library
    val selectedImageUrl = remember {
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=1000&auto=format&fit=crop&q=80"
    }

    var captionText by remember { mutableStateOf("") }
    var selectedAspectRatio by remember { mutableFloatStateOf(1.0f) } // 1:1, 0.8f (4:5), 1.77f (16:9)
    var selectedFilter by remember { mutableStateOf(PostFilter.Normal) }

    var addLocation by remember { mutableStateOf(true) }
    var tagPeople by remember { mutableStateOf(false) }
    var shareToReels by remember { mutableStateOf(false) }

    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VibeBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Top Action Bar (Cancel, New Post title, Share)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cancel",
                color = VibeZinc400,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onNavigateBack() }
            )

            Text(
                text = "New Post",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Text(
                text = "Share",
                color = VibeAccentBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(enabled = !isUploading) {
                    isUploading = true
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    coroutineScope.launch {
                        // 1-second simulated upload progress
                        for (i in 1..10) {
                            delay(100)
                            uploadProgress = i * 0.1f
                        }

                        // Create and prepend post to repository
                        val newPost = Post(
                            id = "post_${UUID.randomUUID()}",
                            author = MockDataProvider.currentUser,
                            mediaUrls = listOf(selectedImageUrl),
                            caption = captionText.ifBlank { "New snapshot from today ✨📸 #Vibe #Photography" },
                            likeCount = 0,
                            isLiked = false,
                            isSaved = false,
                            commentCount = 0,
                            timestamp = "Just now",
                            aspectRatio = selectedAspectRatio,
                            location = if (addLocation) "SoHo, New York" else null
                        )
                        repository.createPost(newPost)
                        onNavigateBack()
                    }
                }
            )
        }

        // Upload progress indicator
        AnimatedVisibility(visible = isUploading) {
            LinearProgressIndicator(
                progress = { uploadProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = VibeAccentBlue,
                trackColor = VibeBorder
            )
        }

        HorizontalDivider(color = VibeBorder, thickness = 0.5.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            // Media Viewport with current aspect ratio and filter
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(selectedAspectRatio)
                    .background(VibeZinc900),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = selectedImageUrl,
                    contentDescription = "Post Preview",
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(selectedFilter.matrix),
                    modifier = Modifier.fillMaxSize()
                )

                // Aspect Ratio Selector overlay (bottom left)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .border(0.5.dp, VibeBorderSubtle, RoundedCornerShape(20.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "1:1" to 1.0f,
                        "4:5" to 0.8f,
                        "16:9" to 1.77f
                    ).forEach { (label, ratio) ->
                        val isSelected = selectedAspectRatio == ratio
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else VibeZinc400,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) VibeBorder else Color.Transparent)
                                .clickable {
                                    selectedAspectRatio = ratio
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Live Color Filter Selector Rail
            Text(
                text = "FILTERS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = VibeZinc500,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(PostFilter.values()) { filter ->
                    val isSelected = filter == selectedFilter
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            selectedFilter = filter
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color.White else VibeBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            AsyncImage(
                                model = selectedImageUrl,
                                contentDescription = filter.label,
                                contentScale = ContentScale.Crop,
                                colorFilter = ColorFilter.colorMatrix(filter.matrix),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = filter.label,
                            fontSize = 11.sp,
                            color = if (isSelected) Color.White else VibeZinc400,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = VibeBorder, thickness = 0.5.dp)

            // Caption input area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                AsyncImage(
                    model = MockDataProvider.currentUser.avatarUrl,
                    contentDescription = "My Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(12.dp))

                TextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    placeholder = {
                        Text(
                            text = "Write a caption... (e.g. #sunset @alex_vibe)",
                            color = VibeZinc500,
                            fontSize = 13.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = VibeAccentBlue
                    ),
                    modifier = Modifier.weight(1f),
                    minLines = 3,
                    maxLines = 6
                )
            }

            HorizontalDivider(color = VibeBorder, thickness = 0.5.dp)

            // Settings & Sharing Toggles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Add Location
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Add Location",
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                    Switch(
                        checked = addLocation,
                        onCheckedChange = { addLocation = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = VibeAccentBlue,
                            uncheckedThumbColor = VibeZinc500,
                            uncheckedTrackColor = VibeZinc900,
                            uncheckedBorderColor = VibeBorder
                        )
                    )
                }

                // Tag People
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Tag People",
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                    Switch(
                        checked = tagPeople,
                        onCheckedChange = { tagPeople = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = VibeAccentBlue,
                            uncheckedThumbColor = VibeZinc500,
                            uncheckedTrackColor = VibeZinc900,
                            uncheckedBorderColor = VibeBorder
                        )
                    )
                }

                // Share to Reels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Movie,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Simultaneous share to Reels",
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                    Switch(
                        checked = shareToReels,
                        onCheckedChange = { shareToReels = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = VibeAccentBlue,
                            uncheckedThumbColor = VibeZinc500,
                            uncheckedTrackColor = VibeZinc900,
                            uncheckedBorderColor = VibeBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
