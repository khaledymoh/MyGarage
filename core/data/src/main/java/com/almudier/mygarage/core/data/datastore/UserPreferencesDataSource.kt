package com.almudier.mygarage.core.data.datastore

import com.almudier.mygarage.core.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesDataSource {

    val userPreferences: Flow<UserPreferences>

    suspend fun current(): UserPreferences

    suspend fun setSignedInUser(uid: String, email: String?, displayName: String?, photoUrl: String?)

    suspend fun update(transform: (UserPreferences) -> UserPreferences)

    suspend fun clear()
}
