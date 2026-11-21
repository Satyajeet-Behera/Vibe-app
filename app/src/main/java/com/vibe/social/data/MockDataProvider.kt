package com.vibe.social.data

import com.vibe.social.model.Comment
import com.vibe.social.model.Post
import com.vibe.social.model.Reel
import com.vibe.social.model.Story
import com.vibe.social.model.User

object MockDataProvider {

    var currentUser = User(
        id = "user_me",
        username = "alex_vibe",
        fullName = "Alex Rivera",
        avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80",
        isVerified = true,
        bio = "Architectural photographer & visual designer 📐✨\nExploring Tokyo, SF & NYC\nDaily creative experiments"
    )

    val users = listOf(
        currentUser,
        User(
            id = "user_elena",
            username = "elena.creates",
            fullName = "Elena Rostova",
            avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500&auto=format&fit=crop&q=80",
            isVerified = true
        ),
        User(
            id = "user_marcus",
            username = "marcus_lens",
            fullName = "Marcus Chen",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop&q=80",
            isVerified = false
        ),
        User(
            id = "user_maya",
            username = "maya_minimal",
            fullName = "Maya Patel",
            avatarUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=500&auto=format&fit=crop&q=80",
            isVerified = true
        ),
        User(
            id = "user_liam",
            username = "liam.nomad",
            fullName = "Liam Vance",
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500&auto=format&fit=crop&q=80",
            isVerified = false
        ),
        User(
            id = "user_sophia",
            username = "sophia.studio",
            fullName = "Sophia Laurent",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500&auto=format&fit=crop&q=80",
            isVerified = true
        )
    )

    val initialStories = listOf(
        // Elena stories (chain of 2)
        Story(
            id = "story_elena_1",
            user = users[1],
            mediaUrl = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=800&auto=format&fit=crop&q=80",
            timestamp = "2h ago",
            isViewed = false
        ),
        Story(
            id = "story_elena_2",
            user = users[1],
            mediaUrl = "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=800&auto=format&fit=crop&q=80",
            timestamp = "1h ago",
            isViewed = false
        ),
        // Marcus stories (chain of 3)
        Story(
            id = "story_marcus_1",
            user = users[2],
            mediaUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800&auto=format&fit=crop&q=80",
            timestamp = "4h ago",
            isViewed = false
        ),
        Story(
            id = "story_marcus_2",
            user = users[2],
            mediaUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop&q=80",
            timestamp = "3h ago",
            isViewed = false
        ),
        Story(
            id = "story_marcus_3",
            user = users[2],
            mediaUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=800&auto=format&fit=crop&q=80",
            timestamp = "2h ago",
            isViewed = false
        ),
        // Maya stories (chain of 2)
        Story(
            id = "story_maya_1",
            user = users[3],
            mediaUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=800&auto=format&fit=crop&q=80",
            timestamp = "5h ago",
            isViewed = false
        ),
        Story(
            id = "story_maya_2",
            user = users[3],
            mediaUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=800&auto=format&fit=crop&q=80",
            timestamp = "3h ago",
            isViewed = false
        ),
        // Sophia stories (chain of 2)
        Story(
            id = "story_sophia_1",
            user = users[5],
            mediaUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800&auto=format&fit=crop&q=80",
            timestamp = "6h ago",
            isViewed = true
        ),
        Story(
            id = "story_sophia_2",
            user = users[5],
            mediaUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=800&auto=format&fit=crop&q=80",
            timestamp = "4h ago",
            isViewed = true
        )
    )

    val sampleCommentsByPostId = mutableMapOf(
        "post_1" to listOf(
            Comment(
                id = "c_1_1",
                author = users[2],
                text = "The lighting in this shot is unreal! 📸 What lens was this?",
                timestamp = "2h ago",
                likeCount = 14,
                isLiked = true
            ),
            Comment(
                id = "c_1_2",
                author = users[3],
                text = "Obsessed with these color tones. Absolute masterpiece 🙌",
                timestamp = "1h ago",
                likeCount = 5,
                isLiked = false
            ),
            Comment(
                id = "c_1_3",
                author = users[5],
                text = "Tokyo always has the best nocturnal vibe 🌃✨",
                timestamp = "30m ago",
                likeCount = 2,
                isLiked = false
            )
        ),
        "post_2" to listOf(
            Comment(
                id = "c_2_1",
                author = users[1],
                text = "Hiking here was the highlight of my year! Stunning view.",
                timestamp = "4h ago",
                likeCount = 8,
                isLiked = true
            ),
            Comment(
                id = "c_2_2",
                author = users[4],
                text = "Nature never ceases to amaze me. Truly wild!",
                timestamp = "3h ago",
                likeCount = 3,
                isLiked = false
            ),
            Comment(
                id = "c_2_3",
                author = users[0],
                text = "Adding this to my bucket list immediately! 🏔️",
                timestamp = "1h ago",
                likeCount = 6,
                isLiked = true
            )
        ),
        "post_3" to listOf(
            Comment(
                id = "c_3_1",
                author = users[2],
                text = "Architectural perfection. Clean geometry at its finest.",
                timestamp = "6h ago",
                likeCount = 9,
                isLiked = false
            ),
            Comment(
                id = "c_3_2",
                author = users[5],
                text = "That shadow fall is so dramatic. Love the brutalist angle.",
                timestamp = "4h ago",
                likeCount = 12,
                isLiked = true
            ),
            Comment(
                id = "c_3_3",
                author = users[1],
                text = "Minimalism done right. ✨",
                timestamp = "2h ago",
                likeCount = 1,
                isLiked = false
            )
        ),
        "post_4" to listOf(
            Comment(
                id = "c_4_1",
                author = users[3],
                text = "The aesthetic of this coffee corner is perfection ☕️",
                timestamp = "8h ago",
                likeCount = 19,
                isLiked = true
            ),
            Comment(
                id = "c_4_2",
                author = users[4],
                text = "Can almost smell the freshly brewed roast from here!",
                timestamp = "5h ago",
                likeCount = 4,
                isLiked = false
            ),
            Comment(
                id = "c_4_3",
                author = users[2],
                text = "Sunday mornings well spent. Cheers! 🥐",
                timestamp = "3h ago",
                likeCount = 7,
                isLiked = false
            )
        ),
        "post_5" to listOf(
            Comment(
                id = "c_5_1",
                author = users[1],
                text = "Pure electric energy! What a night! ⚡️",
                timestamp = "10h ago",
                likeCount = 22,
                isLiked = true
            ),
            Comment(
                id = "c_5_2",
                author = users[0],
                text = "The crowd went wild on that drop! Incredible.",
                timestamp = "7h ago",
                likeCount = 11,
                isLiked = false
            ),
            Comment(
                id = "c_5_3",
                author = users[3],
                text = "Need this set on repeat all weekend 🎧",
                timestamp = "4h ago",
                likeCount = 8,
                isLiked = false
            )
        ),
        "post_6" to listOf(
            Comment(
                id = "c_6_1",
                author = users[4],
                text = "The gradient of this sunset is unreal! Nature's palette 🌅",
                timestamp = "12h ago",
                likeCount = 31,
                isLiked = true
            ),
            Comment(
                id = "c_6_2",
                author = users[2],
                text = "Golden hour done right. Golden reflections everywhere.",
                timestamp = "9h ago",
                likeCount = 15,
                isLiked = false
            ),
            Comment(
                id = "c_6_3",
                author = users[5],
                text = "Breathtaking horizon. California dreaming! 🌴",
                timestamp = "6h ago",
                likeCount = 5,
                isLiked = false
            )
        )
    )

    val initialPosts = listOf(
        Post(
            id = "post_1",
            author = users[1], // Elena
            mediaUrls = listOf("https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=1000&auto=format&fit=crop&q=80"),
            caption = "Lost in the neon labyrinth of Shinjuku. Midnight reflections hit different when the rain clears. 🌧️⚡️ #TokyoNights #CyberVibes #StreetPhotography @marcus_lens",
            likeCount = 1420,
            isLiked = false,
            isSaved = false,
            commentCount = 48,
            timestamp = "2 hours ago",
            aspectRatio = 1.0f,
            location = "Shinjuku, Tokyo"
        ),
        Post(
            id = "post_2",
            author = users[2], // Marcus
            mediaUrls = listOf("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=1000&auto=format&fit=crop&q=80"),
            caption = "First light over the peaks. Woke up at 3:30 AM to capture this mist rolling down the valley. Nature always rewards patience. 🏔️🌤️ #AlpineExplorer #WildernessCulture #LandscapeShots",
            likeCount = 3890,
            isLiked = true,
            isSaved = true,
            commentCount = 124,
            timestamp = "5 hours ago",
            aspectRatio = 0.8f, // 4:5
            location = "Dolomites, Italy"
        ),
        Post(
            id = "post_3",
            author = users[3], // Maya
            mediaUrls = listOf("https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=1000&auto=format&fit=crop&q=80"),
            caption = "Concrete, glass, and soaring angles. Finding rhythm in modern monolithic architecture. Geometry is silent music. 🏢📐 #Brutalist #ModernArchitecture #Minimalism",
            likeCount = 890,
            isLiked = false,
            isSaved = false,
            commentCount = 31,
            timestamp = "8 hours ago",
            aspectRatio = 1.0f,
            location = "San Francisco, CA"
        ),
        Post(
            id = "post_4",
            author = users[0], // Current User Alex
            mediaUrls = listOf("https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=1000&auto=format&fit=crop&q=80"),
            caption = "Slow mornings, warm light, and single-origin pour over. The creative ritual begins here. ☕️📖 #CoffeeCulture #MorningRoutine #CreatorStudio",
            likeCount = 642,
            isLiked = true,
            isSaved = false,
            commentCount = 27,
            timestamp = "11 hours ago",
            aspectRatio = 1.0f,
            location = "SoHo, New York"
        ),
        Post(
            id = "post_5",
            author = users[4], // Liam
            mediaUrls = listOf("https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=1000&auto=format&fit=crop&q=80"),
            caption = "Sub-bass vibrating through the floorboards. Tonight's underground warehouse set was pure transcendence. 🔊🎛️ #LiveMusic #BerlinUnderground #ElectronicBeats",
            likeCount = 2100,
            isLiked = false,
            isSaved = true,
            commentCount = 89,
            timestamp = "14 hours ago",
            aspectRatio = 1.0f,
            location = "Kreuzberg, Berlin"
        ),
        Post(
            id = "post_6",
            author = users[5], // Sophia
            mediaUrls = listOf("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1000&auto=format&fit=crop&q=80"),
            caption = "Pacific horizon melting into lavender dusk. The breeze carries salt and endless summer nostalgia. 🌊🌅 #GoldenHour #OceanBreeze #Wanderlust",
            likeCount = 5320,
            isLiked = false,
            isSaved = false,
            commentCount = 193,
            timestamp = "1 day ago",
            aspectRatio = 1.25f, // 16:9 approx
            location = "Big Sur, California"
        )
    )

    val initialReels = listOf(
        Reel(
            id = "reel_1",
            author = users[1], // Elena
            videoThumbnailUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&auto=format&fit=crop&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            audioTrackName = "Fred again.. & Skrillex • Rumble (Club Mix)",
            caption = "Midnight hyper-lapse running through Shibuya scramble 🏃‍♀️💨 #Tokyo #Reels #Cinematic",
            likeCount = 24500,
            commentCount = 820,
            isLiked = true,
            isFollowing = false
        ),
        Reel(
            id = "reel_2",
            author = users[2], // Marcus
            videoThumbnailUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=800&auto=format&fit=crop&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            audioTrackName = "Bon Iver • Holocene (Acoustic Echo)",
            caption = "Camping under the aurora borealis. The sky started dancing at 1 AM. 🌌🏕️ #Aurora #Iceland #WildCamping",
            likeCount = 89200,
            commentCount = 2400,
            isLiked = false,
            isFollowing = true
        ),
        Reel(
            id = "reel_3",
            author = users[3], // Maya
            videoThumbnailUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=800&auto=format&fit=crop&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            audioTrackName = "Kaytranada • 10% (feat. Kali Uchis)",
            caption = "Behind the scenes: 3D render breakdown of my latest architectural concept! 📐🖥️ #DesignProcess #Blender3D",
            likeCount = 14300,
            commentCount = 310,
            isLiked = true,
            isFollowing = false
        ),
        Reel(
            id = "reel_4",
            author = users[4], // Liam
            videoThumbnailUrl = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=800&auto=format&fit=crop&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
            audioTrackName = "Overmono • So U Kno (Radio Edit)",
            caption = "Analog modular synth jam recorded straight to tape cassette 🎛️📼 #ModularSynth #Eurorack #Vibe",
            likeCount = 41200,
            commentCount = 980,
            isLiked = false,
            isFollowing = false
        )
    )

    val exploreCategories = listOf(
        "For You",
        "Visuals",
        "Architecture",
        "Design",
        "Nature",
        "Style",
        "Tech",
        "Travel",
        "Music"
    )

    data class ExploreItem(
        val id: String,
        val mediaUrl: String,
        val isVideo: Boolean = false,
        val likes: Int = 1200,
        val comments: Int = 45,
        val title: String = ""
    )

    val exploreItems = listOf(
        ExploreItem("exp_1", "https://images.unsplash.com/photo-1518770660439-4636190af475?w=600&auto=format&fit=crop&q=80", false),
        ExploreItem("exp_2", "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600&auto=format&fit=crop&q=80", true),
        ExploreItem("exp_3", "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=600&auto=format&fit=crop&q=80", false),
        ExploreItem("exp_4", "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600&auto=format&fit=crop&q=80", false),
        ExploreItem("exp_5", "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=600&auto=format&fit=crop&q=80", true),
        // Item 6: Featured 2x2 square tile!
        ExploreItem("exp_6", "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=900&auto=format&fit=crop&q=80", false),
        ExploreItem("exp_7", "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=600&auto=format&fit=crop&q=80", false),
        ExploreItem("exp_8", "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=600&auto=format&fit=crop&q=80", false),
        ExploreItem("exp_9", "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=600&auto=format&fit=crop&q=80", false),
        ExploreItem("exp_10", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80", true),
        ExploreItem("exp_11", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&auto=format&fit=crop&q=80", false),
        // Item 12: Featured 2x2 square tile!
        ExploreItem("exp_12", "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=900&auto=format&fit=crop&q=80", true),
        ExploreItem("exp_13", "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=600&auto=format&fit=crop&q=80", false),
        ExploreItem("exp_14", "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&auto=format&fit=crop&q=80", false),
        ExploreItem("exp_15", "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600&auto=format&fit=crop&q=80", true),
        ExploreItem("exp_16", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=600&auto=format&fit=crop&q=80", false),
        ExploreItem("exp_17", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=600&auto=format&fit=crop&q=80", false),
        ExploreItem("exp_18", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=900&auto=format&fit=crop&q=80", false)
    )

    data class Highlight(
        val id: String,
        val title: String,
        val coverUrl: String
    )

    val userHighlights = listOf(
        Highlight("hl_1", "Travel", "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=300&auto=format&fit=crop&q=80"),
        Highlight("hl_2", "Setups", "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=300&auto=format&fit=crop&q=80"),
        Highlight("hl_3", "Art", "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=300&auto=format&fit=crop&q=80"),
        Highlight("hl_4", "Memories", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=300&auto=format&fit=crop&q=80")
    )
}
