package com.vibe.social.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.vibe.social.data.MockDataProvider
import com.vibe.social.navigation.Screen
import com.vibe.social.navigation.VibeNavGraph
import com.vibe.social.theme.VibeBlack
import com.vibe.social.theme.VibeBorder
import com.vibe.social.theme.VibeLightBorder
import com.vibe.social.theme.VibeTextPrimary
import com.vibe.social.theme.VibeZinc400
import com.vibe.social.ui.screens.splash.LoadingScreen

sealed class BottomNavItem(
    val screen: Screen,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val title: String
) {
    object Home : BottomNavItem(Screen.Home, Icons.Filled.Home, Icons.Outlined.Home, "Home")
    object Explore : BottomNavItem(Screen.Explore, Icons.Filled.Search, Icons.Outlined.Search, "Explore")
    object Create : BottomNavItem(Screen.Create, Icons.Filled.AddBox, Icons.Outlined.AddBox, "Create")
    object Reels : BottomNavItem(Screen.Reels, Icons.Filled.Movie, Icons.Outlined.Movie, "Reels")
    object Profile : BottomNavItem(Screen.Profile, Icons.Filled.Home, Icons.Outlined.Home, "Profile")
}

@Composable
fun MainScaffold(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Only show bottom navigation on primary feed tabs
    val showBottomBar = when (currentRoute) {
        Screen.Home.route,
        Screen.Explore.route,
        Screen.Create.route,
        Screen.Reels.route,
        Screen.Profile.route -> true
        else -> false
    }

    val startDestination = remember {
        when (com.vibe.social.data.auth.AuthRepository.instance.authState.value) {
            is com.vibe.social.data.auth.AuthState.Unauthenticated -> Screen.Login.route
            is com.vibe.social.data.auth.AuthState.NeedsProfileSetup -> Screen.ProfileSetup.route
            else -> Screen.Home.route
        }
    }

    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Explore,
        BottomNavItem.Create,
        BottomNavItem.Reels,
        BottomNavItem.Profile
    )

    var isAppLoading by rememberSaveable { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    VibeBottomNavBar(
                        items = bottomNavItems,
                        currentRoute = currentRoute,
                        onItemClick = { item ->
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            },
            containerColor = VibeBlack
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (showBottomBar) innerPadding else androidx.compose.foundation.layout.PaddingValues())
            ) {
                VibeNavGraph(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // App Loading Interface from reference image: pops up whenever user opens the app
        AnimatedVisibility(
            visible = isAppLoading,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(500)),
            modifier = Modifier.fillMaxSize()
        ) {
            LoadingScreen(
                onLoadingFinished = { isAppLoading = false }
            )
        }
    }
}

@Composable
fun VibeBottomNavBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit
) {
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VibeBlack)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(color = VibeBorder, thickness = 0.5.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.screen.route

                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "nav_bounce_${item.title}"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onItemClick(item)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (item == BottomNavItem.Profile) {
                            // Profile tab displays user's circular avatar with thin outline
                            AsyncImage(
                                model = MockDataProvider.currentUser.avatarUrl,
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(26.dp)
                                    .scale(iconScale)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Color.White else VibeLightBorder,
                                        shape = CircleShape
                                    )
                            )
                        } else {
                            Icon(
                                imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                                contentDescription = item.title,
                                tint = if (isSelected) Color.White else VibeZinc400,
                                modifier = Modifier
                                    .size(24.dp)
                                    .scale(iconScale)
                            )
                        }

                        // Subtle active dot indicator below icon
                        Box(
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .size(3.5.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}
