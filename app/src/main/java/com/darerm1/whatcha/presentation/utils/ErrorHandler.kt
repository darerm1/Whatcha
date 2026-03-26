package com.darerm1.whatcha.presentation.utils

import android.content.Context
import com.darerm1.whatcha.R
import com.darerm1.whatcha.domain.common.DomainError

object ErrorHandler {
    fun getErrorMessage(context: Context, error: DomainError): String {
        return when (error) {
            is DomainError.NoInternet -> context.getString(R.string.error_no_internet)
            is DomainError.Timeout -> context.getString(R.string.error_timeout)
            is DomainError.Unauthorized -> context.getString(R.string.error_unauthorized)
            is DomainError.RateLimitExceeded -> context.getString(R.string.error_rate_limit)
            is DomainError.ServerError -> context.getString(R.string.error_server, error.code)
            is DomainError.Unknown -> context.getString(R.string.error_unknown, error.message)
            is DomainError.MovieAlreadyExists -> context.getString(R.string.error_movie_exists)
            is DomainError.MovieNotFound -> context.getString(R.string.error_movie_not_found)
            is DomainError.Validation -> error.message
            is DomainError.MappingFailed -> context.getString(R.string.error_mapping_failed, error.message)
        }
    }
}