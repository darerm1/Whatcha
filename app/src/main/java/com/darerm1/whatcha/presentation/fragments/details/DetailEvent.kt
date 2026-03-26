package com.darerm1.whatcha.presentation.fragments.details

import com.darerm1.whatcha.domain.common.DomainError

sealed class DetailEvent {
    data class ShowSnackbar(val error: DomainError) : DetailEvent()
    object RatingSaved : DetailEvent()
    object StatusUpdated : DetailEvent()
    data class FavoriteToggled(val added: Boolean) : DetailEvent()
}