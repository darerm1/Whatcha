package com.darerm1.whatcha.presentation.fragments.home

import com.darerm1.whatcha.domain.entities.MediaItem

sealed class ListItem {
    data class MovieItem(val movie: MediaItem) : ListItem()
    data object LoadMoreItem : ListItem()
}
