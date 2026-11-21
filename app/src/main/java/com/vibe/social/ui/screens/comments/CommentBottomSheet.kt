package com.vibe.social.ui.screens.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vibe.social.data.MockDataProvider
import com.vibe.social.data.VibeRepository
import com.vibe.social.model.Comment
import com.vibe.social.theme.VibeAccentBlue
import com.vibe.social.theme.VibeBlack
import com.vibe.social.theme.VibeBorder
import com.vibe.social.theme.VibeBorderSubtle
import com.vibe.social.theme.VibeDarkCard
import com.vibe.social.theme.VibeDarkElevated
import com.vibe.social.theme.VibeDarkSurface
import com.vibe.social.theme.VibeLightBorder
import com.vibe.social.theme.VibeLikeRed
import com.vibe.social.theme.VibeTextDisabled
import com.vibe.social.theme.VibeTextPrimary
import com.vibe.social.theme.VibeTextSecondary
import com.vibe.social.theme.VibeTextTertiary
import com.vibe.social.theme.VibeZinc400
import com.vibe.social.theme.VibeZinc500
import com.vibe.social.theme.VibeZinc900
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    postId: String,
    onDismiss: () -> Unit,
    repository: VibeRepository = VibeRepository.instance
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    var commentList by remember(postId) {
        mutableStateOf(repository.getComments(postId))
    }

    var commentText by remember { mutableStateOf("") }
    val quickEmojis = listOf("🔥", "❤️", "😂", "👏", "😢", "😍", "🙌")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = VibeDarkSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(VibeLightBorder)
            )
        },
        modifier = Modifier.fillMaxHeight(0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header title
            Text(
                text = "Comments",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp)
            )

            HorizontalDivider(color = VibeBorderSubtle, thickness = 0.5.dp)

            // Comments list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                if (commentList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No comments yet.\nStart the conversation!",
                                color = VibeZinc500,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(items = commentList, key = { it.id }) { comment ->
                        CommentItemRow(
                            comment = comment,
                            onLikeToggle = {
                                val updated = comment.copy(
                                    isLiked = !comment.isLiked,
                                    likeCount = if (!comment.isLiked) comment.likeCount + 1 else (comment.likeCount - 1).coerceAtLeast(0)
                                )
                                commentList = commentList.map { if (it.id == comment.id) updated else it }
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            HorizontalDivider(color = VibeBorderSubtle, thickness = 0.5.dp)

            // Quick Emoji Rail
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VibeDarkSurface)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                quickEmojis.forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 22.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                commentText += emoji
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            .padding(4.dp)
                    )
                }
            }

            // Comment Input Field Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VibeDarkSurface)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current user avatar
                AsyncImage(
                    model = MockDataProvider.currentUser.avatarUrl,
                    contentDescription = "My Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Input box
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(VibeZinc900)
                        .border(1.dp, VibeBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = {
                            Text(
                                text = "Add a comment...",
                                color = VibeZinc500,
                                fontSize = 12.sp
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
                        singleLine = false,
                        maxLines = 4
                    )

                    val canPost = commentText.isNotBlank()
                    Text(
                        text = "Post",
                        color = if (canPost) VibeAccentBlue else VibeZinc500,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = canPost) {
                                if (canPost) {
                                    val postedComment = repository.addComment(postId, commentText.trim())
                                    commentList = listOf(postedComment) + commentList
                                    commentText = ""
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CommentItemRow(
    comment: Comment,
    onLikeToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = comment.author.avatarUrl,
            contentDescription = "${comment.author.username}'s avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.author.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = comment.timestamp,
                    fontSize = 10.sp,
                    color = VibeZinc500
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = comment.text,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Reply",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VibeZinc400,
                    modifier = Modifier.clickable { }
                )
                if (comment.likeCount > 0) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${comment.likeCount} likes",
                        fontSize = 10.sp,
                        color = VibeZinc400
                    )
                }
            }
        }

        // Like button
        IconButton(
            onClick = onLikeToggle,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = if (comment.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (comment.isLiked) "Unlike" else "Like",
                tint = if (comment.isLiked) VibeLikeRed else VibeZinc500,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
