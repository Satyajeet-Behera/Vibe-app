package com.vibe.social.ui.screens.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vibe.social.data.MockDataProvider
import com.vibe.social.data.messages.DirectMessageRepository
import com.vibe.social.model.Conversation
import com.vibe.social.model.MessageStatus
import com.vibe.social.model.User
import com.vibe.social.theme.InstagramStoryGradientBrush
import com.vibe.social.theme.VibeAccentBlue
import com.vibe.social.theme.VibeBlack
import com.vibe.social.theme.VibeBorder
import com.vibe.social.theme.VibeBorderSubtle
import com.vibe.social.theme.VibeDarkCard
import com.vibe.social.theme.VibeTextPrimary
import com.vibe.social.theme.VibeVerifiedBlue
import com.vibe.social.theme.VibeZinc400
import com.vibe.social.theme.VibeZinc500
import com.vibe.social.theme.VibeZinc800
import com.vibe.social.theme.VibeZinc900
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (conversationId: String) -> Unit,
    modifier: Modifier = Modifier,
    messageRepo: DirectMessageRepository = DirectMessageRepository.instance
) {
    val conversations by messageRepo.conversations.collectAsState()
    val isRealtimeConnected by messageRepo.isRealtimeConnected.collectAsState()
    val currentUser = MockDataProvider.currentUser

    var searchQuery by remember { mutableStateOf("") }
    var showNewChatSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val filteredConversations = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) {
            conversations
        } else {
            messageRepo.searchConversations(searchQuery)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VibeBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("chat_list_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = currentUser.username,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Private messages",
                            tint = VibeZinc500,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Supabase realtime pulse
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(VibeZinc900)
                            .border(0.5.dp, VibeBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isRealtimeConnected) Color(0xFF10B981) else Color(0xFFF59E0B))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Realtime",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = VibeZinc400
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { showNewChatSheet = true },
                        modifier = Modifier.testTag("new_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "New Message",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(VibeZinc900)
                    .border(0.5.dp, VibeBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = VibeZinc500,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(VibeAccentBlue),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_search_input"),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search creators or messages...",
                                    color = VibeZinc500,
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear",
                            tint = VibeZinc500,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                }
            }

            // Active Contacts Horizontal Rail
            Text(
                text = "ACTIVE NOW",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = VibeZinc500,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Filter other creators
                val otherUsers = MockDataProvider.users.filter { it.id != currentUser.id }
                otherUsers.forEach { user ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                val conv = messageRepo.initiateConversation(user)
                                onNavigateToChat(conv.id)
                            }
                            .testTag("active_user_${user.username}")
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = user.fullName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, VibeBorder, CircleShape)
                            )
                            // Online indicator
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                                    .border(2.dp, VibeBlack, CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.fullName.substringBefore(" "),
                            fontSize = 11.sp,
                            color = VibeZinc400,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = VibeBorderSubtle)

            // Conversations List
            Text(
                text = "MESSAGES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = VibeZinc500,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp)
            )

            if (filteredConversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = VibeZinc700(),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No direct messages yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Start a conversation with fellow creators",
                            fontSize = 13.sp,
                            color = VibeZinc500
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredConversations, key = { it.id }) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            currentUserId = currentUser.id,
                            onClick = { onNavigateToChat(conversation.id) }
                        )
                    }
                }
            }
        }

        // New Message Bottom Sheet
        if (showNewChatSheet) {
            ModalBottomSheet(
                onDismissRequest = { showNewChatSheet = false },
                sheetState = sheetState,
                containerColor = VibeZinc900,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .size(width = 36.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(VibeZinc700())
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "New Message",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Select a creator to start chatting",
                        fontSize = 13.sp,
                        color = VibeZinc400,
                        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                    )

                    val creators = MockDataProvider.users.filter { it.id != currentUser.id }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                    ) {
                        items(creators, key = { it.id }) { creator ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        coroutineScope.launch {
                                            sheetState.hide()
                                            showNewChatSheet = false
                                            val conv = messageRepo.initiateConversation(creator)
                                            onNavigateToChat(conv.id)
                                        }
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = creator.avatarUrl,
                                    contentDescription = creator.fullName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = creator.fullName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        if (creator.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Filled.Verified,
                                                contentDescription = "Verified",
                                                tint = VibeVerifiedBlue,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "@${creator.username}",
                                        fontSize = 12.sp,
                                        color = VibeZinc500
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(VibeAccentBlue)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Chat",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ConversationItem(
    conversation: Conversation,
    currentUserId: String,
    onClick: () -> Unit
) {
    val participant = conversation.participant
    val lastMessage = conversation.lastMessage
    val hasUnread = conversation.unreadCount > 0
    val isFromMe = lastMessage?.senderId == currentUserId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("conversation_${participant.username}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Participant Avatar with Online Indicator
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = participant.avatarUrl,
                contentDescription = participant.fullName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
            )
            if (conversation.isOnline) {
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                        .border(2.dp, VibeBlack, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Center: Name & Last Message
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = participant.fullName,
                    fontSize = 14.sp,
                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold,
                    color = Color.White
                )
                if (participant.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Verified",
                        tint = VibeVerifiedBlue,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Subtitle: Typing indicator or last message snippet
            if (conversation.isTyping) {
                Text(
                    text = "typing...",
                    fontSize = 13.sp,
                    color = VibeAccentBlue,
                    fontWeight = FontWeight.Medium
                )
            } else if (lastMessage != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isFromMe) {
                        // Read receipt indicator
                        when (lastMessage.status) {
                            MessageStatus.READ -> {
                                Icon(
                                    imageVector = Icons.Filled.DoneAll,
                                    contentDescription = "Read",
                                    tint = VibeAccentBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            MessageStatus.DELIVERED -> {
                                Icon(
                                    imageVector = Icons.Filled.DoneAll,
                                    contentDescription = "Delivered",
                                    tint = VibeZinc500,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            MessageStatus.SENT -> {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Sent",
                                    tint = VibeZinc500,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            MessageStatus.SENDING -> {
                                Text("•", color = VibeZinc500, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Text(
                        text = if (isFromMe) "You: ${lastMessage.text}" else lastMessage.text,
                        fontSize = 13.sp,
                        fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal,
                        color = if (hasUnread) Color.White else VibeZinc500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Text(
                    text = "Active now • Say hello",
                    fontSize = 12.sp,
                    color = VibeZinc500
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right: Timestamp & Unread Badge
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatConversationTimestamp(conversation.updatedAt),
                fontSize = 11.sp,
                color = if (hasUnread) VibeAccentBlue else VibeZinc500,
                fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal
            )

            if (hasUnread) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(VibeAccentBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = conversation.unreadCount.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun formatConversationTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60000
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "now"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}d"
        else -> "1w"
    }
}

@Composable
private fun VibeZinc700(): Color = Color(0xFF3F3F46)
