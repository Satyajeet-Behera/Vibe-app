package com.vibe.social.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vibe.social.data.MockDataProvider
import com.vibe.social.data.VibeRepository
import com.vibe.social.data.VibeRepositoryImpl
import com.vibe.social.model.Post
import com.vibe.social.theme.VibeAccentBlue
import com.vibe.social.theme.VibeBlack
import com.vibe.social.theme.VibeBorder
import com.vibe.social.theme.VibeBorderSubtle
import com.vibe.social.theme.VibeDarkCard
import com.vibe.social.theme.VibeDarkSurface
import com.vibe.social.theme.VibeLightBorder
import com.vibe.social.theme.VibeTextDisabled
import com.vibe.social.theme.VibeTextPrimary
import com.vibe.social.theme.VibeTextSecondary
import com.vibe.social.theme.VibeTextTertiary
import com.vibe.social.theme.VibeVerifiedBlue
import com.vibe.social.theme.VibeZinc300
import com.vibe.social.theme.VibeZinc400
import com.vibe.social.theme.VibeZinc500
import com.vibe.social.theme.VibeZinc900

enum class ProfileTab(val title: String) {
    Posts("Posts"),
    Reels("Reels"),
    Saved("Saved")
}

@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit = {},
    onNavigateToProfileSetup: () -> Unit = {},
    modifier: Modifier = Modifier,
    repository: VibeRepository = VibeRepositoryImpl.instance
) {
    val user = MockDataProvider.currentUser
    val allPosts by repository.posts.collectAsState()
    val allReels by repository.reels.collectAsState()

    // Posts authored by currentUser (or feed items for demo richness)
    val userPosts = remember(allPosts) {
        val authored = allPosts.filter { it.author.id == user.id }
        if (authored.isNotEmpty()) authored else allPosts.take(4)
    }

    // Saved posts
    val savedPosts = remember(allPosts) {
        allPosts.filter { it.isSaved }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showDiscoverPeopleSheet by remember { mutableStateOf(false) }
    val tabs = ProfileTab.values()
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VibeBlack)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Profile Header spans all 3 columns
            item(span = { GridItemSpan(3) }) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Profile Top Bar (username, lock icon, menu)
                    ProfileTopBar(
                        username = user.username,
                        onLogoutClick = onLogoutClick,
                        onNavigateToProfileSetup = onNavigateToProfileSetup
                    )

                    // Avatar & Stats Row
                    ProfileStatsSection(user = user, postCount = userPosts.size)

                    // Bio Section
                    ProfileBioSection(user = user)

                    // Action Buttons (Edit Profile, Share Profile, Add Person)
                    ProfileActionsRow(
                        onEditProfileClick = onNavigateToProfileSetup,
                        onShareProfileClick = {
                            android.widget.Toast.makeText(context, "Profile link copied: vibe.social/${user.username}", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        onDiscoverPeopleClick = {
                            showDiscoverPeopleSheet = true
                        }
                    )

                    // Highlights Carousel
                    HighlightsSection()

                    // Content Tabs (Grid, Reels, Saved)
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = VibeBlack,
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = Color.White,
                                height = 1.5.dp
                            )
                        },
                        divider = {
                            HorizontalDivider(color = VibeBorder, thickness = 0.5.dp)
                        }
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            val isSelected = selectedTabIndex == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTabIndex = index },
                                icon = {
                                    Icon(
                                        imageVector = when (tab) {
                                            ProfileTab.Posts -> if (isSelected) Icons.Filled.GridOn else Icons.Outlined.GridOn
                                            ProfileTab.Reels -> if (isSelected) Icons.Filled.Movie else Icons.Outlined.Movie
                                            ProfileTab.Saved -> if (isSelected) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder
                                        },
                                        contentDescription = tab.title,
                                        tint = if (isSelected) Color.White else VibeZinc500,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Grid Items based on active tab
            when (tabs[selectedTabIndex]) {
                ProfileTab.Posts -> {
                    if (userPosts.isEmpty()) {
                        item(span = { GridItemSpan(3) }) {
                            EmptyTabContent(message = "No posts yet")
                        }
                    } else {
                        items(userPosts, key = { it.id }) { post ->
                            GridMediaTile(imageUrl = post.mediaUrls.firstOrNull() ?: "")
                        }
                    }
                }
                ProfileTab.Reels -> {
                    items(allReels, key = { it.id }) { reel ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.8f)
                                .background(VibeDarkSurface)
                        ) {
                            AsyncImage(
                                model = reel.videoThumbnailUrl,
                                contentDescription = "Reel",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${reel.likeCount / 1000}k",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                ProfileTab.Saved -> {
                    if (savedPosts.isEmpty()) {
                        item(span = { GridItemSpan(3) }) {
                            EmptyTabContent(message = "No saved posts yet.\nBookmark posts to see them here.")
                        }
                    } else {
                        items(savedPosts, key = { it.id }) { post ->
                            GridMediaTile(imageUrl = post.mediaUrls.firstOrNull() ?: "")
                        }
                    }
                }
            }
        }

        if (showDiscoverPeopleSheet) {
            DiscoverPeopleBottomSheet(
                onDismiss = { showDiscoverPeopleSheet = false }
            )
        }
    }
}

@Composable
fun ProfileTopBar(
    username: String,
    onLogoutClick: () -> Unit = {},
    onNavigateToProfileSetup: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val authRepo = com.vibe.social.data.auth.AuthRepository.instance

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Private account",
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = username,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.testTag("profile_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            androidx.compose.material3.DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier
                    .background(VibeZinc900)
                    .border(1.dp, VibeBorder, RoundedCornerShape(8.dp))
            ) {
                androidx.compose.material3.DropdownMenuItem(
                    text = {
                        Text(
                            text = "Edit Profile",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    },
                    onClick = {
                        showMenu = false
                        onNavigateToProfileSetup()
                    }
                )

                HorizontalDivider(color = VibeBorder)

                androidx.compose.material3.DropdownMenuItem(
                    text = {
                        Text(
                            text = "Log Out",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    },
                    onClick = {
                        showMenu = false
                        coroutineScope.launch {
                            authRepo.logout()
                            onLogoutClick()
                        }
                    },
                    modifier = Modifier.testTag("logout_menu_item")
                )
            }
        }
    }
}

@Composable
fun ProfileStatsSection(user: com.vibe.social.model.User, postCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Large circular avatar with subtle border
        AsyncImage(
            model = user.avatarUrl,
            contentDescription = "Profile Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(1.dp, VibeLightBorder, CircleShape)
        )

        Spacer(modifier = Modifier.width(20.dp))

        // Stats Counters Row
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatCounter(count = "$postCount", label = "Posts")
            StatCounter(count = "14.8k", label = "Followers")
            StatCounter(count = "492", label = "Following")
        }
    }
}

@Composable
fun StatCounter(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = VibeZinc400
        )
    }
}

@Composable
fun ProfileBioSection(user: com.vibe.social.model.User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = user.fullName.ifBlank { "Alex Rivera" },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )
            if (user.isVerified) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Verified",
                    tint = VibeAccentBlue,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Text(
            text = "Digital Creator",
            fontSize = 11.sp,
            color = VibeZinc400,
            modifier = Modifier.padding(vertical = 1.dp)
        )

        Text(
            text = user.bio.ifBlank {
                "Architectural photographer & visual designer 📐✨\nExploring Tokyo, SF & NYC\nDaily creative experiments"
            },
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = Color.White,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        // External Link
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Link,
                contentDescription = "Website",
                tint = VibeAccentBlue,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "vibe.social/${user.username}",
                color = VibeAccentBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ProfileActionsRow(
    onEditProfileClick: () -> Unit = {},
    onShareProfileClick: () -> Unit = {},
    onDiscoverPeopleClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Edit Profile Button - Professional Polish (Zinc900 container, 1px border)
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(VibeZinc900)
                .border(1.dp, VibeBorder, RoundedCornerShape(8.dp))
                .clickable { onEditProfileClick() }
                .padding(vertical = 7.dp)
                .testTag("edit_profile_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Edit Profile",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        // Share Profile Button
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(VibeZinc900)
                .border(1.dp, VibeBorder, RoundedCornerShape(8.dp))
                .clickable { onShareProfileClick() }
                .padding(vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Share Profile",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        // Add Contact / Discover people Icon Button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(VibeZinc900)
                .border(1.dp, VibeBorder, RoundedCornerShape(8.dp))
                .clickable { onDiscoverPeopleClick() }
                .padding(7.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PersonAdd,
                contentDescription = "Discover people",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun HighlightsSection() {
    val highlights = MockDataProvider.userHighlights
    val context = androidx.compose.ui.platform.LocalContext.current

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(highlights) { highlight ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(62.dp)
                    .clickable {
                        android.widget.Toast.makeText(context, "Highlight: ${highlight.title}", android.widget.Toast.LENGTH_SHORT).show()
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(1.dp, VibeLightBorder, CircleShape)
                        .padding(2.dp)
                ) {
                    AsyncImage(
                        model = highlight.coverUrl,
                        contentDescription = highlight.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = highlight.title,
                    fontSize = 10.sp,
                    color = VibeZinc300,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // "+ New" Highlight
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(62.dp)
                    .clickable {
                        android.widget.Toast.makeText(context, "Add new highlight to profile", android.widget.Toast.LENGTH_SHORT).show()
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(1.dp, VibeLightBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Highlight",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "New",
                    fontSize = 10.sp,
                    color = VibeZinc400
                )
            }
        }
    }
}

@Composable
fun GridMediaTile(imageUrl: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.0f)
            .background(VibeZinc900)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Post thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun EmptyTabContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = VibeTextSecondary,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DiscoverPeopleBottomSheet(
    onDismiss: () -> Unit
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val suggestedUsers = MockDataProvider.users.filter { it.id != MockDataProvider.currentUser.id }
    val followedUserIds = remember { androidx.compose.runtime.mutableStateListOf<String>() }

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = com.vibe.social.theme.VibeZinc900
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Discover People",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            suggestedUsers.forEach { suggestedUser ->
                val isFollowing = followedUserIds.contains(suggestedUser.id)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        AsyncImage(
                            model = suggestedUser.avatarUrl,
                            contentDescription = suggestedUser.username,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = suggestedUser.username,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                if (suggestedUser.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = com.vibe.social.theme.VibeAccentBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Text(
                                text = suggestedUser.fullName,
                                color = com.vibe.social.theme.VibeZinc400,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isFollowing) com.vibe.social.theme.VibeZinc800 else com.vibe.social.theme.VibeAccentBlue
                            )
                            .clickable {
                                if (isFollowing) {
                                    followedUserIds.remove(suggestedUser.id)
                                } else {
                                    followedUserIds.add(suggestedUser.id)
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isFollowing) "Following" else "Follow",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                HorizontalDivider(color = com.vibe.social.theme.VibeBorderSubtle, thickness = 0.5.dp)
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
