package com.almudier.mygarage.core.domain

sealed interface NetworkError {
    data object NoConnection : NetworkError
    data object Timeout : NetworkError
    data object Unauthorized : NetworkError
    data object PermissionDenied : NetworkError
    data object NotFound : NetworkError
    data object TooManyRequests : NetworkError
    data object Server : NetworkError
    data object Unknown : NetworkError
}

class NetworkException(val error: NetworkError, cause: Throwable? = null) : Exception(cause)
