package com.almudier.mygarage.core.data.datastore

import androidx.datastore.core.DataStore
import com.almudier.mygarage.core.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

class UserPreferencesDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<UserPreferences>,
) : UserPreferencesDataSource {

    override val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { throwable -> if (throwable is IOException) emit(UserPreferences()) else throw throwable }

    override suspend fun current(): UserPreferences = userPreferences.first()

    override suspend fun setSignedInUser(
        uid: String,
        email: String?,
        displayName: String?,
        photoUrl: String?,
    ) {
        dataStore.updateData {
            it.copy(
                uid = uid,
                email = email,
                displayName = displayName,
                photoUrl = photoUrl,
                isSignedIn = true,
            )
        }
    }

    override suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        dataStore.updateData(transform)
    }

    override suspend fun clear() {
        dataStore.updateData { UserPreferences() }
    }
}
