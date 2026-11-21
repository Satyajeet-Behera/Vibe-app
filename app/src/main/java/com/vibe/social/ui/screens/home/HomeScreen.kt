package com.vibe.social.ui.screens.home

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.R
import com.vibe.social.data.MockDataProvider
import com.vibe.social.data.messages.DirectMessageRepository
import com.vibe.social.model.Post
import com.vibe.social.model.Story
import com.vibe.social.theme.VibeAccentBlue
import com.vibe.social.theme.VibeBlack
import com.vibe.social.theme.VibeBorder
import com.vibe.social.theme.VibeBorderSubtle
import com.vibe.social.theme.VibeDarkSurface
import com.vibe.social.theme.VibeLikeRed
import com.vibe.social.theme.VibeTextPrimary
import com.vibe.social.theme.VibeZinc300
import com.vibe.social.theme.VibeZinc400
import com.vibe.social.theme.VibeZinc500
import com.vibe.social.theme.VibeZinc900
import com.vibe.social.ui.components.DoubleTapHeartOverlay
import com.vibe.social.ui.components.ExpandableCaption
import com.vibe.social.ui.components.StoryAvatarRing
import com.vibe.social.ui.screens.comments.CommentBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToStory: (userId: String) -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNotificationsSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VibeBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Home Top App Bar
            HomeTopBar(
                onNavigateToMessages = onNavigateToMessages,
                onNotificationsClick = { showNotificationsSheet = true }
            )

            // Feed & Stories
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Stories Horizontal Rail
                item(key = "stories_rail") {
                    StoriesRail(
                        stories = uiState.stories,
                        onStoryClick = { userId -> onNavigateToStory(userId) },
                        onAddStoryClick = onNavigateToCreate
                    )
                    HorizontalDivider(
                        color = VibeBorderSubtle,
                        thickness = 0.5.dp
                    )
                }

                // Feed Posts
                items(
                    items = uiState.posts,
                    key = { it.id }
                ) { post ->
                    PostFeedItem(
                        post = post,
                        onLikeClick = { viewModel.toggleLike(post.id) },
                        onSaveClick = { viewModel.toggleSave(post.id) },
                        onCommentClick = { viewModel.openComments(post.id) },
                        onDoubleTapMedia = {
                            if (!post.isLiked) {
                                viewModel.toggleLike(post.id)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Active Comments Bottom Sheet
        uiState.activeCommentPostId?.let { postId ->
            CommentBottomSheet(
                postId = postId,
                onDismiss = { viewModel.closeComments() }
            )
        }

        // Notifications Bottom Sheet
        if (showNotificationsSheet) {
            NotificationsSheet(
                onDismiss = { showNotificationsSheet = false }
            )
        }
    }
}

@Composable
fun HomeTopBar(
    onNavigateToMessages: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    val conversations by DirectMessageRepository.instance.conversations.collectAsState()
    val totalUnreadCount = remember(conversations) {
        conversations.sumOf { it.unreadCount }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "Vibe" Brand Logo with App Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_vibe_logo),
                contentDescription = "Vibe Logo",
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Text(
                text = "Vibe",
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                letterSpacing = (-0.5).sp,
                color = Color.White
            )
        }

        // Top Right Actions (Heart notification + DM Plane)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Heart Notification with red unread dot and black border
            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier
                    .clickable { onNotificationsClick() }
                    .testTag("home_notifications_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "Notifications",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(VibeLikeRed)
                        .border(1.5.dp, VibeBlack, CircleShape)
                )
            }

            // Direct Messages with unread badge counter and black border
            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier
                    .clickable { onNavigateToMessages() }
                    .testTag("home_dm_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Messages",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(-20f)
                )
                if (totalUnreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(VibeLikeRed)
                            .border(1.5.dp, VibeBlack, RoundedCornerShape(8.dp))
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (totalUnreadCount > 99) "99+" else "$totalUnreadCount",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StoriesRail(
    stories: List<Story>,
    onStoryClick: (String) -> Unit,
    onAddStoryClick: () -> Unit
) {
    val distinctUserStories = remember(stories) {
        val userMap = mutableMapOf<String, com.vibe.social.model.User>()
        val viewedMap = mutableMapOf<String, Boolean>()

        stories.forEach { story ->
            userMap[story.user.id] = story.user
            val currentViewed = viewedMap[story.user.id] ?: true
            viewedMap[story.user.id] = currentViewed && story.isViewed
        }

        userMap.values.map { user ->
            val allViewed = viewedMap[user.id] ?: false
            Pair(user, allViewed)
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(VibeDarkSurface)
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Current user story / add story item
        item(key = "current_user_story") {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(68.dp)
            ) {
                StoryAvatarRing(
                    avatarUrl = MockDataProvider.currentUser.avatarUrl,
                    size = 64.dp,
                    isCurrentUser = true,
                    showAddBadge = true,
                    hasStory = false,
                    onClick = onAddStoryClick
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Your Story",
                    fontSize = 10.sp,
                    color = VibeZinc400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 2. Followed users stories
        items(
            items = distinctUserStories,
            key = { it.first.id }
        ) { (user, allViewed) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(68.dp)
            ) {
                StoryAvatarRing(
                    avatarUrl = user.avatarUrl,
                    size = 64.dp,
                    isViewed = allViewed,
                    hasStory = true,
                    onClick = { onStoryClick(user.id) }
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = user.username,
                    fontSize = 10.sp,
                    color = if (allViewed) VibeZinc500 else VibeZinc300,
                    fontStyle = if (allViewed) FontStyle.Italic else FontStyle.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PostFeedItem(
    post: Post,
    onLikeClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCommentClick: () -> Unit,
    onDoubleTapMedia: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    var heartAnimTrigger by remember { mutableLongStateOf(0L) }
    var menuExpanded by remember { mutableStateOf(false) }

    val likeScale by animateFloatAsState(
        targetValue = if (post.isLiked) 1.25f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "post_like_scale"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Post Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                StoryAvatarRing(
                    avatarUrl = post.author.avatarUrl,
                    size = 32.dp,
                    borderWidth = 1.5.dp,
                    hasStory = true,
                    isViewed = false,
                    onClick = { }
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.author.username,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        if (post.author.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Verified",
                                tint = VibeAccentBlue,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    if (post.location != null) {
                        Text(
                            text = post.location,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = VibeZinc400,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Options",
                        tint = VibeZinc400,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(VibeZinc900)
                ) {
                    DropdownMenuItem(
                        text = { Text("Share post", color = VibeTextPrimary) },
                        onClick = {
                            menuExpanded = false
                            Toast.makeText(context, "Post link ready to share!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Copy link", color = VibeTextPrimary) },
                        onClick = {
                            menuExpanded = false
                            Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Hide post", color = VibeZinc400) },
                        onClick = {
                            menuExpanded = false
                            Toast.makeText(context, "Post hidden from feed", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Post Media with direct pointerInput Double-Tap Heart Burst
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(post.aspectRatio)
                .background(VibeZinc900)
                .pointerInput(post.id) {
                    detectTapGestures(
                        onDoubleTap = {
                            heartAnimTrigger = System.currentTimeMillis()
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDoubleTapMedia()
                        }
                    )
                }
        ) {
            AsyncImage(
                model = post.mediaUrls.firstOrNull() ?: "",
                contentDescription = "Post image by ${post.author.username}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Double tap glowing heart burst
            DoubleTapHeartOverlay(
                trigger = heartAnimTrigger,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Action Bar (Like, Comment, Share, Save)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Like Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onLikeClick()
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (post.isLiked) "Unlike" else "Like",
                        tint = if (post.isLiked) VibeLikeRed else Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(likeScale)
                    )
                }

                // Comment Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = onCommentClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Share Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            Toast.makeText(context, "Post link copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }

            // Save / Bookmark Button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSaveClick()
                    }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (post.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (post.isSaved) "Remove from saved" else "Save",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Like Count
        if (post.likeCount > 0) {
            Text(
                text = "${post.likeCount} likes",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )
        }

        // Caption with clickable expansion and bold author
        ExpandableCaption(
            username = post.author.username,
            caption = post.caption,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )

        // View comments button
        if (post.commentCount > 0) {
            Text(
                text = "View all ${post.commentCount} comments",
                fontSize = 12.sp,
                color = VibeZinc500,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .clickable { onCommentClick() }
            )
        }

        // Timestamp
        Text(
            text = post.timestamp.uppercase(),
            fontSize = 10.sp,
            letterSpacing = 0.5.sp,
            color = VibeZinc500,
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val notifications = remember {
        listOf(
            Triple(MockDataProvider.users[1], "liked your photo.", "2m ago"),
            Triple(MockDataProvider.users[2], "started following you.", "1h ago"),
            Triple(MockDataProvider.users[3], "commented: \"Stunning capture! 🔥\"", "3h ago"),
            Triple(MockDataProvider.users[4], "liked your reel.", "5h ago"),
            Triple(MockDataProvider.users[0], "tagged you in a story.", "1d ago")
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = VibeZinc900
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifications",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            notifications.forEach { (user, actionText, time) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = user.username,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${user.username} $actionText",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Text(
                                text = time,
                                color = VibeZinc400,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(VibeAccentBlue)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Follow",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                HorizontalDivider(color = VibeBorderSubtle, thickness = 0.5.dp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
