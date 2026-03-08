package com.darerm1.whatcha.ui.fragments.home

import com.darerm1.whatcha.data.interfaces.MediaItem

sealed class ListItem {
    data class MovieItem(val movie: MediaItem) : ListItem()
    data object LoadMoreItem : ListItem()
}
