package com.almudier.mygarage.core.data.network

import com.almudier.mygarage.core.domain.NetworkError
import com.almudier.mygarage.core.domain.NetworkException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.toNetworkException(): NetworkException {
    if (this is NetworkException) return this
    val error = when (this) {
        is FirebaseNetworkException, is UnknownHostException -> NetworkError.NoConnection
        is SocketTimeoutException -> NetworkError.Timeout
        is FirebaseTooManyRequestsException -> NetworkError.TooManyRequests
        is FirebaseAuthException -> when (errorCode) {
            "ERROR_NETWORK_REQUEST_FAILED" -> NetworkError.NoConnection
            "ERROR_TOO_MANY_REQUESTS" -> NetworkError.TooManyRequests
            "ERROR_USER_TOKEN_EXPIRED", "ERROR_INVALID_USER_TOKEN",
            "ERROR_INVALID_CREDENTIAL", "ERROR_USER_DISABLED" -> NetworkError.Unauthorized
            else -> NetworkError.Unknown
        }
        is FirebaseFirestoreException -> when (code) {
            FirebaseFirestoreException.Code.UNAVAILABLE -> NetworkError.NoConnection
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> NetworkError.Timeout
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> NetworkError.Unauthorized
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> NetworkError.PermissionDenied
            FirebaseFirestoreException.Code.NOT_FOUND -> NetworkError.NotFound
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> NetworkError.TooManyRequests
            FirebaseFirestoreException.Code.INTERNAL,
            FirebaseFirestoreException.Code.DATA_LOSS -> NetworkError.Server
            else -> NetworkError.Unknown
        }
        is IOException -> NetworkError.NoConnection
        else -> NetworkError.Unknown
    }
    return NetworkException(error, this)
}
