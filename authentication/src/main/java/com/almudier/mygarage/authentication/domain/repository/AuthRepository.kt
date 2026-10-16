package com.almudier.mygarage.authentication.domain.repository

import android.content.Context
import com.almudier.mygarage.authentication.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val signedInUser: Flow<AuthUser?>

    suspend fun signInWithGoogle(activityContext: Context): Result<AuthUser>

    suspend fun signOut()
}
