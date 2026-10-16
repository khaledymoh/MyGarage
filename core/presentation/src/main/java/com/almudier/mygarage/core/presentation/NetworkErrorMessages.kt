package com.almudier.mygarage.core.presentation

import androidx.annotation.StringRes
import com.almudier.mygarage.core.domain.NetworkError
import com.almudier.mygarage.core.domain.NetworkException
import com.almudier.mygarage.resources.R

@StringRes
fun Throwable.toErrorMessageRes(): Int =
    (this as? NetworkException)?.error?.toMessageRes() ?: R.string.error_unknown

@StringRes
fun NetworkError.toMessageRes(): Int = when (this) {
    NetworkError.NoConnection -> R.string.error_no_connection
    NetworkError.Timeout -> R.string.error_timeout
    NetworkError.Unauthorized -> R.string.error_unauthorized
    NetworkError.PermissionDenied -> R.string.error_permission_denied
    NetworkError.NotFound -> R.string.error_not_found
    NetworkError.TooManyRequests -> R.string.error_too_many_requests
    NetworkError.Server -> R.string.error_server
    NetworkError.Unknown -> R.string.error_unknown
}
