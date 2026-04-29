package com.darerm1.whatcha.presentation.sdui

import com.darerm1.whatcha.data.sdui.models.SDUIAnalytics

sealed class SDUIItem(val viewType: Int) {

    data class MovieCardItem(
        val title: String,
        val yearGenre: String,
        val posterUrl: String?,
        val isFavorite: Boolean,
        val movieId: Long?,
        val analytics: SDUIAnalytics?
    ) : SDUIItem(VIEW_TYPE_MOVIE_CARD)

    data class PrimaryButtonItem(
        val buttonText: String,
        val action: String?,
        val analytics: SDUIAnalytics?
    ) : SDUIItem(VIEW_TYPE_PRIMARY_BUTTON)

    data class RatingBarItem(
        val rating: Float,
        val editable: Boolean,
        val analytics: SDUIAnalytics?
    ) : SDUIItem(VIEW_TYPE_RATING_BAR)

    data class SearchInputItem(
        val query: String,
        val hint: String,
        val analytics: SDUIAnalytics?
    ) : SDUIItem(VIEW_TYPE_SEARCH_INPUT)

    data class ErrorViewItem(
        val errorText: String,
        val buttonText: String,
        val showButton: Boolean,
        val analytics: SDUIAnalytics?
    ) : SDUIItem(VIEW_TYPE_ERROR_VIEW)

    data class StatusChipItem(
        val status: String,
        val chipText: String,
        val analytics: SDUIAnalytics?
    ) : SDUIItem(VIEW_TYPE_STATUS_CHIP)

    data class SecondaryButtonItem(
        val buttonText: String,
        val action: String?,
        val shareText: String?,
        val analytics: SDUIAnalytics?
    ) : SDUIItem(VIEW_TYPE_SECONDARY_BUTTON)

    companion object {
        const val VIEW_TYPE_MOVIE_CARD = 0
        const val VIEW_TYPE_PRIMARY_BUTTON = 1
        const val VIEW_TYPE_RATING_BAR = 2
        const val VIEW_TYPE_SEARCH_INPUT = 3
        const val VIEW_TYPE_ERROR_VIEW = 4
        const val VIEW_TYPE_STATUS_CHIP = 5
        const val VIEW_TYPE_SECONDARY_BUTTON = 6
    }
}
