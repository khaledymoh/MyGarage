package com.almudier.mygarage.authentication.data.remote

import android.content.Context
import com.almudier.mygarage.authentication.data.model.UserDto

interface AuthRemoteDataSource {

    suspend fun getGoogleIdToken(activityContext: Context): Result<String>

    suspend fun signInWithGoogleIdToken(idToken: String): Result<UserDto>

    suspend fun signOut()
}
