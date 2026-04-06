package com.darerm1.whatcha.domain.common

sealed class DomainError {
    object NoInternet : DomainError()
    object Timeout : DomainError()
    object Unauthorized : DomainError()
    object RateLimitExceeded : DomainError()
    data class ServerError(val code: Int) : DomainError()
    data class Unknown(val message: String?) : DomainError()
    object MovieAlreadyExists : DomainError()
    object MovieNotFound : DomainError()
    data class Validation(val message: String) : DomainError()
    data class MappingFailed(val message: String) : DomainError()
}