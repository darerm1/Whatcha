package com.darerm1.whatcha.presentation.fragments.favorites.viewmodel

import com.darerm1.whatcha.domain.common.DomainError
import com.darerm1.whatcha.domain.entities.MediaItem
import com.darerm1.whatcha.domain.entities.enums.Status

sealed class FavoritesState {
    object Loading : FavoritesState()
    data class Content(
        val movies: List<MediaItem>,
        val currentSort: String,
        val currentSortAscending: Boolean,
        val currentFilter: Status?
    ) : FavoritesState()
    data class Error(val error: DomainError) : FavoritesState()
}