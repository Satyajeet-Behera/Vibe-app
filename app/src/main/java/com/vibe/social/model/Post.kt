package com.vibe.social.model

import androidx.compose.runtime.Immutable

@Immutable
data class Post(
    val id: String,
    val author: User,
    val mediaUrls: List<String>,
    val caption: String,
    val likeCount: Int,
    val isLiked: Boolean,
    val isSaved: Boolean,
    val commentCount: Int,
    val timestamp: String,
    val aspectRatio: Float = 1.0f,
    val location: String? = null
)
