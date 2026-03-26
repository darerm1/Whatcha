package com.darerm1.whatcha.presentation.fragments.home.viewmodel

import com.darerm1.whatcha.domain.common.DomainError
import com.darerm1.whatcha.domain.entities.MediaItem

sealed class HomeState {
    object Loading : HomeState()
    data class Content(
        val movies: List<MediaItem>,
        val hasMore: Boolean,
        val isSearchMode: Boolean
    ) : HomeState()
    data class Error(val error: DomainError) : HomeState()
}