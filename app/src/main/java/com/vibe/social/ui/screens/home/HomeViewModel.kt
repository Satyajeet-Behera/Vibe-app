package com.vibe.social.ui.screens.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibe.social.data.VibeRepository
import com.vibe.social.data.VibeRepositoryImpl
import com.vibe.social.model.Post
import com.vibe.social.model.Story
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@Immutable
data class HomeUiState(
    val posts: List<Post> = emptyList(),
    val stories: List<Story> = emptyList(),
    val activeCommentPostId: String? = null
)

class HomeViewModel(
    private val repository: VibeRepository = VibeRepositoryImpl.instance
) : ViewModel() {

    private val _activeCommentPostId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.posts,
        repository.stories,
        _activeCommentPostId
    ) { posts, stories, activeCommentId ->
        HomeUiState(
            posts = posts,
            stories = stories,
            activeCommentPostId = activeCommentId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun toggleLike(postId: String) {
        repository.toggleLike(postId)
    }

    fun toggleSave(postId: String) {
        repository.toggleSave(postId)
    }

    fun openComments(postId: String) {
        _activeCommentPostId.value = postId
    }

    fun closeComments() {
        _activeCommentPostId.value = null
    }
}
