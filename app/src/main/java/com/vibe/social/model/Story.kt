package com.vibe.social.model

import androidx.compose.runtime.Immutable

@Immutable
data class Story(
    val id: String,
    val user: User,
    val mediaUrl: String,
    val timestamp: String,
    val isViewed: Boolean = false
)
