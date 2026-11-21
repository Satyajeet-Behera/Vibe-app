package com.vibe.social.model

import androidx.compose.runtime.Immutable

@Immutable
data class User(
    val id: String,
    val username: String,
    val fullName: String,
    val avatarUrl: String,
    val isVerified: Boolean = false,
    val bio: String = "",
    val email: String = ""
)
