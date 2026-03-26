package com.darerm1.whatcha.presentation.common

import com.darerm1.whatcha.domain.entities.MediaItem

sealed class MoviesUiState {
    object Loading : MoviesUiState()
    data class Success(val movies: List<MediaItem>, val hasMore: Boolean) : MoviesUiState()
    data class Error(val message: String, val canRetry: Boolean = true) : MoviesUiState()
    object Empty : MoviesUiState()
}
