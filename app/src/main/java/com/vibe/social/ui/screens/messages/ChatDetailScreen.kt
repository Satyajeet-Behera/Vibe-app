package com.vibe.social.ui.screens.messages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vibe.social.data.MockDataProvider
import com.vibe.social.data.messages.DirectMessageRepository
import com.vibe.social.model.Conversation
import com.vibe.social.model.DirectMessage
import com.vibe.social.model.MessageStatus
import com.vibe.social.theme.VibeAccentBlue
import com.vibe.social.theme.VibeBlack
import com.vibe.social.theme.VibeBorder
import com.vibe.social.theme.VibeBorderSubtle
import com.vibe.social.theme.VibeDarkCard
import com.vibe.social.theme.VibeVerifiedBlue
import com.vibe.social.theme.VibeZinc400
import com.vibe.social.theme.VibeZinc500
import com.vibe.social.theme.VibeZinc800
import com.vibe.social.theme.VibeZinc900
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatDetailScreen(
    conversationId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    messageRepo: DirectMessageRepository = DirectMessageRepository.instance
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()

    val conversations by messageRepo.conversations.collectAsState()
    val conversation = remember(conversations, conversationId) {
        conversations.firstOrNull { it.id == conversationId } ?: messageRepo.getConversation(conversationId)
    }

    val messagesFlow = remember(conversationId) {
        messageRepo.getMessagesForConversation(conversationId)
    }
    val messages by messagesFlow.collectAsState()

    val currentUserId = MockDataProvider.currentUser.id
    var inputText by remember { mutableStateOf("") }
    var showEmojiBar by remember { mutableStateOf(false) }

    // Auto mark as read on open
    LaunchedEffect(conversationId, messages.size) {
        messageRepo.markConversationAsRead(conversationId)
    }

    // Auto scroll to latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val participant = conversation?.participant ?: MockDataProvider.users[1]

    fun handleSend(textToSend: String = inputText) {
        val trimmed = textToSend.trim()
        if (trimmed.isEmpty()) return

        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        inputText = ""

        coroutineScope.launch {
            messageRepo.sendMessage(conversationId, trimmed)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VibeBlack)
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(VibeBlack)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Box(contentAlignment = Alignment.BottomEnd) {
                        AsyncImage(
                            model = participant.avatarUrl,
                            contentDescription = participant.fullName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                        if (conversation?.isOnline == true) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                                    .border(1.5.dp, VibeBlack, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = participant.fullName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
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

                        Text(
                            text = if (conversation?.isTyping == true) "typing..." else if (conversation?.isOnline == true) "Active now" else "@${participant.username}",
                            fontSize = 11.sp,
                            color = if (conversation?.isTyping == true) VibeAccentBlue else VibeZinc500,
                            fontWeight = if (conversation?.isTyping == true) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { /* Audio Call */ }) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Audio call",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = { /* Video Call */ }) {
                        Icon(
                            imageVector = Icons.Filled.Videocam,
                            contentDescription = "Video call",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = VibeBorderSubtle)

            // Messages LazyColumn
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    // Profile Header in Chat
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = participant.avatarUrl,
                            contentDescription = participant.fullName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .border(1.dp, VibeBorder, CircleShape)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = participant.fullName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "@${participant.username} • Vibe Creator",
                            fontSize = 12.sp,
                            color = VibeZinc500
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(
                            modifier = Modifier.width(120.dp),
                            color = VibeBorder
                        )
                    }
                }

                items(messages, key = { it.id }) { message ->
                    val isFromMe = message.senderId == currentUserId
                    MessageBubble(
                        message = message,
                        isFromMe = isFromMe,
                        participant = participant
                    )
                }

                // Typing Bubble
                if (conversation?.isTyping == true) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            AsyncImage(
                                model = participant.avatarUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(VibeDarkCard)
                                    .border(0.5.dp, VibeBorder, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "• • •",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VibeZinc400
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Quick Emoji Drawer
            AnimatedVisibility(
                visible = showEmojiBar,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VibeZinc900)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val emojis = listOf("❤️", "🔥", "🙌", "✨", "👏", "😍", "🎉", "💯")
                    emojis.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 22.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    handleSend(emoji)
                                    showEmojiBar = false
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }

            // Bottom Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VibeBlack)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attach image action
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(VibeZinc900)
                        .clickable { /* Photo attachment */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = "Attach photo",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Input Box
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(VibeZinc900)
                        .border(0.5.dp, VibeBorder, RoundedCornerShape(22.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(VibeAccentBlue),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input"),
                        decorationBox = { innerTextField ->
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "Message...",
                                    color = VibeZinc500,
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    IconButton(
                        onClick = { showEmojiBar = !showEmojiBar },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SentimentSatisfiedAlt,
                            contentDescription = "Emoji",
                            tint = if (showEmojiBar) VibeAccentBlue else VibeZinc500,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) VibeAccentBlue else VibeZinc900)
                        .clickable(enabled = inputText.isNotBlank()) {
                            handleSend()
                        }
                        .testTag("chat_send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) Color.White else VibeZinc500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: DirectMessage,
    isFromMe: Boolean,
    participant: com.vibe.social.model.User
) {
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) {
        timeFormatter.format(Date(message.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isFromMe) {
            AsyncImage(
                model = participant.avatarUrl,
                contentDescription = participant.fullName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isFromMe) 16.dp else 4.dp,
                            bottomEnd = if (isFromMe) 4.dp else 16.dp
                        )
                    )
                    .background(if (isFromMe) VibeAccentBlue else VibeDarkCard)
                    .border(
                        width = 0.5.dp,
                        color = if (isFromMe) Color.Transparent else VibeBorder,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isFromMe) 16.dp else 4.dp,
                            bottomEnd = if (isFromMe) 4.dp else 16.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = Color.White,
                    lineHeight = 19.sp
                )
            }

            // Timestamp and Read Receipts
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp, end = 2.dp)
            ) {
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = VibeZinc500
                )

                if (isFromMe) {
                    Spacer(modifier = Modifier.width(4.dp))
                    when (message.status) {
                        MessageStatus.READ -> {
                            Icon(
                                imageVector = Icons.Filled.DoneAll,
                                contentDescription = "Seen",
                                tint = VibeAccentBlue,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        MessageStatus.DELIVERED -> {
                            Icon(
                                imageVector = Icons.Filled.DoneAll,
                                contentDescription = "Delivered",
                                tint = VibeZinc500,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        MessageStatus.SENT -> {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Sent",
                                tint = VibeZinc500,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        MessageStatus.SENDING -> {
                            Text("•", color = VibeZinc500, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
