package com.darerm1.whatcha.presentation.fragments.favorites.viewmodel

import com.darerm1.whatcha.domain.entities.MediaItem
import com.darerm1.whatcha.domain.entities.enums.Status

sealed class FavoritesIntent {
    object Load : FavoritesIntent()
    data class Sort(val sortBy: String, val ascending: Boolean) : FavoritesIntent()
    data class Filter(val status: Status?) : FavoritesIntent()
    data class RemoveMovie(val movie: MediaItem) : FavoritesIntent()
}