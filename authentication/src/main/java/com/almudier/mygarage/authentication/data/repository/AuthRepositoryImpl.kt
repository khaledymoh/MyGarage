package com.almudier.mygarage.authentication.data.repository

import android.content.Context
import com.almudier.mygarage.authentication.data.mapper.toDomain
import com.almudier.mygarage.authentication.data.mapper.toDomainOrNull
import com.almudier.mygarage.authentication.data.remote.AuthRemoteDataSource
import com.almudier.mygarage.authentication.domain.model.AuthUser
import com.almudier.mygarage.authentication.domain.repository.AuthRepository
import com.almudier.mygarage.core.data.datastore.UserPreferencesDataSource
import com.almudier.mygarage.core.data.network.FirebaseNetworkCaller
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val networkCaller: FirebaseNetworkCaller,
): AuthRepository {

    override val signedInUser: Flow<AuthUser?> =
        userPreferencesDataSource.userPreferences.map { it.toDomainOrNull() }

    override suspend fun signInWithGoogle(activityContext: Context): Result<AuthUser> {
        networkCaller.ensureOnline().onFailure { return Result.failure(it) }
        return remoteDataSource.getGoogleIdToken(activityContext)
            .mapCatching { idToken -> remoteDataSource.signInWithGoogleIdToken(idToken).getOrThrow() }
            .onSuccess { dto ->
                userPreferencesDataSource.setSignedInUser(
                    uid = dto.uid,
                    email = dto.email,
                    displayName = dto.displayName,
                    photoUrl = dto.photoUrl,
                )
            }
            .map { it.toDomain() }
    }

    override suspend fun signOut() {
        remoteDataSource.signOut()
        userPreferencesDataSource.clear()
    }
}
