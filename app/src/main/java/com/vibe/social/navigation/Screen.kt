package com.vibe.social.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ProfileSetup : Screen("profile_setup")

    object Home : Screen("home")
    object Explore : Screen("explore")
    object Create : Screen("create")
    object Reels : Screen("reels")
    object Profile : Screen("profile")

    object DirectMessages : Screen("direct_messages")
    object ChatDetail : Screen("chat_detail/{conversationId}") {
        fun createRoute(conversationId: String): String = "chat_detail/$conversationId"
    }

    object StoryViewer : Screen("story_viewer/{userId}") {
        fun createRoute(userId: String): String = "story_viewer/$userId"
    }

    object CommentsSheet : Screen("comments_sheet/{postId}") {
        fun createRoute(postId: String): String = "comments_sheet/$postId"
    }
}
