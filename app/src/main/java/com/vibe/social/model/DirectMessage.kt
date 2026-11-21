package com.vibe.social.model

import androidx.compose.runtime.Immutable

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

@Immutable
data class DirectMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val mediaUrl: String? = null
)

@Immutable
data class Conversation(
    val id: String,
    val participant: User,
    val lastMessage: DirectMessage? = null,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isTyping: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
