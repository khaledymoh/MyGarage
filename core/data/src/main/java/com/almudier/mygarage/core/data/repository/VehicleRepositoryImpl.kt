package com.almudier.mygarage.core.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import com.almudier.mygarage.core.data.datastore.UserPreferencesDataSource
import com.almudier.mygarage.core.data.network.FirebaseNetworkCaller
import com.almudier.mygarage.core.data.network.closeWithListenerError
import com.almudier.mygarage.core.domain.NetworkError
import com.almudier.mygarage.core.domain.NetworkException
import com.almudier.mygarage.core.domain.model.Vehicle
import com.almudier.mygarage.core.domain.repository.VehicleRepository
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    @param:ApplicationContext private val context: Context,
    private val userPreferences: UserPreferencesDataSource,
    private val networkCaller: FirebaseNetworkCaller,
) : VehicleRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeVehicles(): Flow<List<Vehicle>> =
        userPreferences.userPreferences
            .map { it.uid }
            .distinctUntilChanged()
            .flatMapLatest { uid ->
                if (uid == null) flowOf(emptyList()) else vehiclesFlow(vehiclesOf(uid))
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeVehicle(id: String): Flow<Vehicle?> =
        userPreferences.userPreferences
            .map { it.uid }
            .distinctUntilChanged()
            .flatMapLatest { uid ->
                // Signed out is not "car deleted": stay silent so screens don't react to a missing car.
                if (uid == null) emptyFlow() else vehicleFlow(vehiclesOf(uid).document(id))
            }

    override suspend fun deleteVehicle(id: String): Result<Unit> {
        val uid = userPreferences.current().uid
            ?: return Result.failure(NetworkException(NetworkError.Unauthorized))
        val document = vehiclesOf(uid).document(id)
        return networkCaller.call {
            val photoUri = document.get().await().getString("photoUri")
            val batch = firestore.batch()
            document.collection(MAINTENANCE).get().await().documents.forEach {
                batch.delete(it.reference.collection("attachments").document("receipt"))
                batch.delete(it.reference)
            }
            batch.delete(document)
            batch.commit().await()
            deleteLocalPhoto(photoUri)
        }
    }

    private suspend fun deleteLocalPhoto(photoUri: String?) {
        val uri = photoUri?.let(Uri::parse) ?: return
        if (uri.scheme != "file") return
        withContext(Dispatchers.IO) { uri.path?.let { File(it).delete() } }
    }

    private fun vehicleFlow(document: DocumentReference): Flow<Vehicle?> = callbackFlow {
        val registration = document.addSnapshotListener { snapshot, error ->
            if (error != null) {
                closeWithListenerError(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.takeIf { it.exists() }?.toVehicle())
        }
        awaitClose { registration.remove() }
    }

    override suspend fun saveVehicle(vehicle: Vehicle): Result<Vehicle> {
        val uid = userPreferences.current().uid
            ?: return Result.failure(NetworkException(NetworkError.Unauthorized))
        val collection = vehiclesOf(uid)
        val isNew = vehicle.id.isBlank()
        val document = if (isNew) collection.document() else collection.document(vehicle.id)
        return networkCaller.call {
            val photoBase64 = encodePhoto(vehicle.photoUri)
            val saved = vehicle.copy(
                id = document.id,
                photoUri = photoBase64?.let { PhotoEncoder.DATA_URI_PREFIX + it },
            )
            val base = saved.toMap() + (FIELD_PHOTO to photoBase64)
            val data = if (isNew) base + (FIELD_CREATED_AT to FieldValue.serverTimestamp()) else base
            document.set(data, SetOptions.merge()).await()
            saved
        }
    }

    private suspend fun encodePhoto(photoUri: String?): String? {
        if (photoUri.isNullOrBlank()) return null
        if (photoUri.startsWith(PhotoEncoder.DATA_URI_PREFIX)) return photoUri.removePrefix(PhotoEncoder.DATA_URI_PREFIX)
        val uri = Uri.parse(photoUri)
        if (uri.scheme != "content" && uri.scheme != "file") return null
        return withContext(Dispatchers.IO) { PhotoEncoder.encode(context, uri) }
    }

    private fun vehiclesOf(uid: String): CollectionReference =
        firestore.collection(USERS).document(uid).collection(VEHICLES)

    private fun vehiclesFlow(collection: CollectionReference): Flow<List<Vehicle>> = callbackFlow {
        val registration = collection
            .orderBy(FIELD_CREATED_AT)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    closeWithListenerError(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents.orEmpty().mapNotNull { it.toVehicle() })
            }
        awaitClose { registration.remove() }
    }

    private fun Vehicle.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "manufacturer" to manufacturer,
        "model" to model,
        "year" to year,
        "trim" to trim,
        "mileage" to mileage,
        "plate" to plate,
        "vin" to vin,
        "color" to color,
        "photoUri" to null,
    )

    private fun DocumentSnapshot.toVehicle(): Vehicle? {
        return Vehicle(
            id = id,
            manufacturer = getString("manufacturer") ?: return null,
            model = getString("model") ?: return null,
            year = getLong("year")?.toInt() ?: 0,
            trim = getString("trim").orEmpty(),
            mileage = getLong("mileage")?.toInt() ?: 0,
            plate = getString("plate").orEmpty(),
            vin = getString("vin").orEmpty(),
            color = getString("color").orEmpty(),
            photoUri = getString(FIELD_PHOTO)?.takeIf { it.isNotBlank() }?.let { PhotoEncoder.DATA_URI_PREFIX + it }
                ?: getString("photoUri"),
        )
    }

    private companion object {
        const val USERS = "users"
        const val VEHICLES = "vehicles"
        const val MAINTENANCE = "maintenance"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_PHOTO = "photoBase64"
    }
}
