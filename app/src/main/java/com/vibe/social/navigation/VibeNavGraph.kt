package com.vibe.social.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vibe.social.data.auth.AuthRepository
import com.vibe.social.data.auth.AuthState
import com.vibe.social.ui.screens.auth.LoginScreen
import com.vibe.social.ui.screens.auth.ProfileSetupScreen
import com.vibe.social.ui.screens.auth.RegisterScreen
import com.vibe.social.ui.screens.create.CreatePostScreen
import com.vibe.social.ui.screens.explore.ExploreScreen
import com.vibe.social.ui.screens.home.HomeScreen
import com.vibe.social.ui.screens.messages.ChatDetailScreen
import com.vibe.social.ui.screens.messages.ChatListScreen
import com.vibe.social.ui.screens.profile.ProfileScreen
import com.vibe.social.ui.screens.reels.ReelsScreen
import com.vibe.social.ui.screens.story.StoryViewerScreen

@Composable
fun VibeNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(220)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(220)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(220)) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(220)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(220)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(220)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(220)) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(220)
            )
        }
    ) {
        // Authentication Flow
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToProfileSetup = {
                    navController.navigate(Screen.ProfileSetup.route)
                }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegistrationSuccess = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                onSetupComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Main Navigation Screens
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToStory = { userId ->
                    navController.navigate(Screen.StoryViewer.createRoute(userId))
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToMessages = {
                    navController.navigate(Screen.DirectMessages.route)
                },
                onNavigateToCreate = {
                    navController.navigate(Screen.Create.route)
                }
            )
        }

        composable(route = Screen.Explore.route) {
            ExploreScreen()
        }

        composable(route = Screen.Create.route) {
            CreatePostScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Reels.route) {
            ReelsScreen(
                onNavigateToCreate = {
                    navController.navigate(Screen.Create.route)
                }
            )
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(
                onLogoutClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToProfileSetup = {
                    navController.navigate(Screen.ProfileSetup.route)
                }
            )
        }

        // Direct Messaging Flow
        composable(route = Screen.DirectMessages.route) {
            ChatListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToChat = { conversationId ->
                    navController.navigate(Screen.ChatDetail.createRoute(conversationId))
                }
            )
        }

        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(
                navArgument("conversationId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            ChatDetailScreen(
                conversationId = conversationId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Story Viewer
        composable(
            route = Screen.StoryViewer.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    defaultValue = "user_elena"
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "user_elena"
            StoryViewerScreen(
                userId = userId,
                onClose = {
                    navController.popBackStack()
                }
            )
        }
    }
}
