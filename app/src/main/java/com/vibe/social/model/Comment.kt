package com.vibe.social.model

import androidx.compose.runtime.Immutable

@Immutable
data class Comment(
    val id: String,
    val author: User,
    val text: String,
    val timestamp: String,
    val likeCount: Int = 0,
    val isLiked: Boolean = false
)
