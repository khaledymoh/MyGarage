package com.almudier.mygarage.core.data.repository

import com.almudier.mygarage.core.data.datastore.UserPreferencesDataSource
import com.almudier.mygarage.core.domain.repository.MaintenanceServiceRepository
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaintenanceServiceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userPreferences: UserPreferencesDataSource,
) : MaintenanceServiceRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeServices(): Flow<List<String>> =
        userPreferences.userPreferences
            .map { it.uid }
            .distinctUntilChanged()
            .flatMapLatest { uid ->
                if (uid == null) flowOf(DEFAULT_SERVICES) else servicesFlow(servicesOf(uid))
            }

    private fun servicesFlow(collection: CollectionReference): Flow<List<String>> = callbackFlow {
        val registration = collection.addSnapshotListener { snapshot, error ->
            val custom = if (error != null || snapshot == null) {
                emptyList()
            } else {
                snapshot.documents
                    .filter { it.getBoolean(FIELD_CUSTOM) == true }
                    .mapNotNull { it.getString(FIELD_NAME) }
                    .sorted()
            }
            trySend((DEFAULT_SERVICES + custom).distinctBy { it.lowercase() })
        }
        awaitClose { registration.remove() }
    }

    override suspend fun addServices(names: List<String>) {
        val uid = userPreferences.current().uid ?: return
        val collection = servicesOf(uid)
        val batch = firestore.batch()
        names.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }.forEach { name ->
            val id = "custom_" + name.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
            batch.set(
                collection.document(id),
                mapOf(FIELD_NAME to name, FIELD_CUSTOM to true),
                SetOptions.merge(),
            )
        }
        runCatching { batch.commit() }
    }

    private fun servicesOf(uid: String): CollectionReference =
        firestore.collection(USERS).document(uid).collection(SERVICES)

    private companion object {
        const val USERS = "users"
        const val SERVICES = "maintenanceServices"
        const val FIELD_NAME = "name"
        const val FIELD_CUSTOM = "custom"
    }
}
