package com.vibe.social.model

import androidx.compose.runtime.Immutable

@Immutable
data class Reel(
    val id: String,
    val author: User,
    val videoThumbnailUrl: String,
    val videoUrl: String = "",
    val audioTrackName: String,
    val caption: String,
    val likeCount: Int,
    val commentCount: Int,
    val isLiked: Boolean,
    val isSaved: Boolean = false,
    val isFollowing: Boolean = false
)
