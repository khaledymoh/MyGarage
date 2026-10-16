package com.almudier.mygarage.core.data.network

import com.almudier.mygarage.core.domain.NetworkError
import com.almudier.mygarage.core.domain.NetworkException
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseNetworkCaller @Inject constructor(
    private val connectivityChecker: ConnectivityChecker,
) {
    fun ensureOnline(): Result<Unit> =
        if (connectivityChecker.isOnline()) {
            Result.success(Unit)
        } else {
            Result.failure(NetworkException(NetworkError.NoConnection))
        }

    suspend fun <T> call(block: suspend () -> T): Result<T> {
        ensureOnline().onFailure { return Result.failure(it) }
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e.toNetworkException())
        }
    }
}
