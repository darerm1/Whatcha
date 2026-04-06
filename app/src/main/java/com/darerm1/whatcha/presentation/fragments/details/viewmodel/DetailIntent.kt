package com.darerm1.whatcha.presentation.fragments.details.viewmodel

import com.darerm1.whatcha.domain.entities.enums.Status

sealed class DetailIntent {
    data class Load(val movieId: Long) : DetailIntent()
    object ToggleFavorite : DetailIntent()
    data class UpdateStatus(val status: Status) : DetailIntent()
    data class UpdateRating(val rating: Float) : DetailIntent()
}