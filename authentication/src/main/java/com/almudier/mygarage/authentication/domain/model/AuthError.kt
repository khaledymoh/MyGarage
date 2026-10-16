package com.almudier.mygarage.authentication.domain.model

sealed interface AuthError {
    data object Cancelled : AuthError
    data object NoGoogleAccount : AuthError
    data object NoPlayServices : AuthError
    data class Unknown(val message: String?) : AuthError
}

class AuthException(val error: AuthError, cause: Throwable? = null) : Exception(cause)
