package com.darerm1.whatcha.utils

import android.content.Context
import com.darerm1.whatcha.R
import com.darerm1.whatcha.data.common.NetworkError

object NetworkErrorHandler {
    fun getErrorMessage(context: Context, error: NetworkError): String {
        return when (error) {
            is NetworkError.NoInternet -> context.getString(R.string.error_no_internet)
            is NetworkError.Timeout -> context.getString(R.string.error_timeout)
            is NetworkError.RateLimitExceeded -> context.getString(R.string.error_rate_limit)
            is NetworkError.Unauthorized -> context.getString(R.string.error_unauthorized)
            is NetworkError.ServerError -> context.getString(R.string.error_server, error.code)
            is NetworkError.Unknown -> context.getString(R.string.error_unknown, error.message)
        }
    }
}
