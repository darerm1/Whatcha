package com.darerm1.whatcha.data.common

sealed class NetworkError {
    object NoInternet : NetworkError()
    object Timeout : NetworkError()
    object Unauthorized : NetworkError()
    object RateLimitExceeded : NetworkError()
    data class ServerError(val code: Int) : NetworkError()
    data class Unknown(val message: String?) : NetworkError()
}
