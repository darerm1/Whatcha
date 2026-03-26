package com.darerm1.whatcha.presentation.fragments.details.viewmodel

import com.darerm1.whatcha.domain.common.DomainError
import com.darerm1.whatcha.domain.entities.Movie
import com.darerm1.whatcha.domain.entities.enums.Status

sealed class DetailState {
    object Loading : DetailState()
    data class Content(
        val movie: Movie,
        val isFavorite: Boolean,
        val personalRating: Float?,
        val status: Status
    ) : DetailState()
    data class Error(val error: DomainError) : DetailState()
}