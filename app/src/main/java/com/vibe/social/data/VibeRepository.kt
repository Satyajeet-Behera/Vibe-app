package com.vibe.social.data

import com.vibe.social.model.Comment
import com.vibe.social.model.Post
import com.vibe.social.model.Reel
import com.vibe.social.model.Story
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

interface VibeRepository {
    val posts: StateFlow<List<Post>>
    val stories: StateFlow<List<Story>>
    val reels: StateFlow<List<Reel>>

    fun toggleLike(postId: String)
    fun toggleSave(postId: String)
    fun addComment(postId: String, text: String): Comment
    fun getComments(postId: String): List<Comment>
    fun createPost(post: Post)
    fun viewStory(storyId: String)
    fun toggleReelLike(reelId: String)
    fun toggleReelFollow(reelId: String)

    companion object {
        val instance: VibeRepository by lazy { VibeRepositoryImpl.instance }
    }
}

class VibeRepositoryImpl private constructor() : VibeRepository {

    private val _posts = MutableStateFlow<List<Post>>(MockDataProvider.initialPosts)
    override val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _stories = MutableStateFlow<List<Story>>(MockDataProvider.initialStories)
    override val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    private val _reels = MutableStateFlow<List<Reel>>(MockDataProvider.initialReels)
    override val reels: StateFlow<List<Reel>> = _reels.asStateFlow()

    private val _comments = MutableStateFlow<Map<String, List<Comment>>>(MockDataProvider.sampleCommentsByPostId)

    override fun toggleLike(postId: String) {
        _posts.update { currentPosts ->
            currentPosts.map { post ->
                if (post.id == postId) {
                    val newIsLiked = !post.isLiked
                    val newLikeCount = if (newIsLiked) post.likeCount + 1 else (post.likeCount - 1).coerceAtLeast(0)
                    post.copy(isLiked = newIsLiked, likeCount = newLikeCount)
                } else {
                    post
                }
            }
        }
    }

    override fun toggleSave(postId: String) {
        _posts.update { currentPosts ->
            currentPosts.map { post ->
                if (post.id == postId) {
                    post.copy(isSaved = !post.isSaved)
                } else {
                    post
                }
            }
        }
    }

    override fun addComment(postId: String, text: String): Comment {
        val newComment = Comment(
            id = "c_${UUID.randomUUID()}",
            author = MockDataProvider.currentUser,
            text = text,
            timestamp = "Just now",
            likeCount = 0,
            isLiked = false
        )

        // Optimistically update comments map
        _comments.update { currentMap ->
            val existing = currentMap[postId] ?: emptyList()
            currentMap + (postId to (listOf(newComment) + existing))
        }

        // Optimistically increment post commentCount
        _posts.update { currentPosts ->
            currentPosts.map { post ->
                if (post.id == postId) {
                    post.copy(commentCount = post.commentCount + 1)
                } else {
                    post
                }
            }
        }

        return newComment
    }

    override fun getComments(postId: String): List<Comment> {
        return _comments.value[postId] ?: emptyList()
    }

    override fun createPost(post: Post) {
        _posts.update { currentPosts ->
            listOf(post) + currentPosts
        }
    }

    override fun viewStory(storyId: String) {
        _stories.update { currentStories ->
            currentStories.map { story ->
                if (story.id == storyId) {
                    story.copy(isViewed = true)
                } else {
                    story
                }
            }
        }
    }

    override fun toggleReelLike(reelId: String) {
        _reels.update { currentReels ->
            currentReels.map { reel ->
                if (reel.id == reelId) {
                    val newLiked = !reel.isLiked
                    val newCount = if (newLiked) reel.likeCount + 1 else (reel.likeCount - 1).coerceAtLeast(0)
                    reel.copy(isLiked = newLiked, likeCount = newCount)
                } else {
                    reel
                }
            }
        }
    }

    override fun toggleReelFollow(reelId: String) {
        _reels.update { currentReels ->
            currentReels.map { reel ->
                if (reel.id == reelId) {
                    reel.copy(isFollowing = !reel.isFollowing)
                } else {
                    reel
                }
            }
        }
    }

    companion object {
        val instance: VibeRepository by lazy { VibeRepositoryImpl() }
    }
}
