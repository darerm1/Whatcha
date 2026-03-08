package com.darerm1.whatcha.ui.common

import com.darerm1.whatcha.data.interfaces.MediaItem

sealed class MoviesUiState {
    object Loading : MoviesUiState()
    data class Success(val movies: List<MediaItem>, val hasMore: Boolean) : MoviesUiState()
    data class Error(val message: String, val canRetry: Boolean = true) : MoviesUiState()
    object Empty : MoviesUiState()
}
