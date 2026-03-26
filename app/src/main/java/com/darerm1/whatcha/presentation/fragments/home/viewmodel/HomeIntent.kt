package com.darerm1.whatcha.presentation.fragments.home.viewmodel

import com.darerm1.whatcha.domain.entities.MediaItem

sealed class HomeIntent {
    object Load : HomeIntent()
    data class Search(val query: String) : HomeIntent()
    object LoadMore : HomeIntent()
    data class ToggleFavorite(val movie: MediaItem) : HomeIntent()
}