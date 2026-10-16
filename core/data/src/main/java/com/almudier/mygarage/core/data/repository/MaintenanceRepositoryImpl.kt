package com.almudier.mygarage.core.data.repository

import android.content.Context
import android.net.Uri
import com.almudier.mygarage.core.data.datastore.UserPreferencesDataSource
import com.almudier.mygarage.core.data.network.FirebaseNetworkCaller
import com.almudier.mygarage.core.data.network.closeWithListenerError
import com.almudier.mygarage.core.domain.NetworkError
import com.almudier.mygarage.core.domain.NetworkException
import com.almudier.mygarage.core.domain.model.MaintenanceItem
import com.almudier.mygarage.core.domain.model.MaintenanceRecord
import com.almudier.mygarage.core.domain.model.RecordType
import com.almudier.mygarage.core.domain.repository.MaintenanceRepository
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaintenanceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    @param:ApplicationContext private val context: Context,
    private val userPreferences: UserPreferencesDataSource,
    private val networkCaller: FirebaseNetworkCaller,
) : MaintenanceRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeRecords(vehicleId: String): Flow<List<MaintenanceRecord>> =
        uidFlow().flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList()) else recordsFlow(recordsOf(uid, vehicleId), vehicleId)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeRecord(vehicleId: String, recordId: String): Flow<MaintenanceRecord?> =
        uidFlow().flatMapLatest { uid ->
            // Signed out is not "record deleted": stay silent so screens don't react to a missing record.
            if (uid == null) emptyFlow() else recordFlow(recordsOf(uid, vehicleId), vehicleId, recordId)
        }

    override suspend fun saveRecord(record: MaintenanceRecord): Result<MaintenanceRecord> {
        val uid = userPreferences.current().uid
            ?: return Result.failure(NetworkException(NetworkError.Unauthorized))
        val collection = recordsOf(uid, record.vehicleId)
        val isNew = record.id.isBlank()
        val document = if (isNew) collection.document() else collection.document(record.id)
        return networkCaller.call {
            val receiptRef = document.collection(ATTACHMENTS).document(RECEIPT)
            val newReceipt = encodeNewReceipt(record.receipt)
            val hasReceipt = newReceipt != null || record.receipt != null || record.hasReceipt
            val saved = record.copy(id = document.id, hasReceipt = hasReceipt, receipt = null)
            val data = if (isNew) {
                saved.toMap() + (FIELD_CREATED_AT to FieldValue.serverTimestamp())
            } else {
                saved.toMap()
            }
            val vehicleDoc = firestore.collection(USERS).document(uid)
                .collection(VEHICLES).document(record.vehicleId)
            val currentMileage = vehicleDoc.get().await().getLong("mileage") ?: 0L
            val batch = firestore.batch()
            batch.set(document, data, SetOptions.merge())
            when {
                newReceipt != null -> batch.set(receiptRef, mapOf(FIELD_DATA to newReceipt))
                !hasReceipt && !isNew -> batch.delete(receiptRef)
            }
            if (record.mileage > currentMileage) {
                batch.set(vehicleDoc, mapOf("mileage" to record.mileage), SetOptions.merge())
            }
            batch.commit().await()
            saved
        }
    }

    override suspend fun deleteRecord(vehicleId: String, recordId: String): Result<Unit> {
        val uid = userPreferences.current().uid
            ?: return Result.failure(NetworkException(NetworkError.Unauthorized))
        return networkCaller.call {
            val document = recordsOf(uid, vehicleId).document(recordId)
            val batch = firestore.batch()
            batch.delete(document.collection(ATTACHMENTS).document(RECEIPT))
            batch.delete(document)
            batch.commit().await()
        }
    }

    override suspend fun loadReceipt(vehicleId: String, recordId: String): Result<String?> {
        val uid = userPreferences.current().uid
            ?: return Result.failure(NetworkException(NetworkError.Unauthorized))
        return networkCaller.call {
            recordsOf(uid, vehicleId).document(recordId)
                .collection(ATTACHMENTS).document(RECEIPT)
                .get().await()
                .getString(FIELD_DATA)
                ?.let { PhotoEncoder.DATA_URI_PREFIX + it }
        }
    }

    /** Base64 for a newly picked image (content/file URI); null when unchanged or removed. */
    private suspend fun encodeNewReceipt(receipt: String?): String? {
        if (receipt.isNullOrBlank() || receipt.startsWith("data:")) return null
        val uri = Uri.parse(receipt)
        if (uri.scheme != "content" && uri.scheme != "file") return null
        return withContext(Dispatchers.IO) { PhotoEncoder.encode(context, uri) }
    }

    private fun uidFlow(): Flow<String?> =
        userPreferences.userPreferences.map { it.uid }.distinctUntilChanged()

    private fun recordsOf(uid: String, vehicleId: String): CollectionReference =
        firestore.collection(USERS).document(uid)
            .collection(VEHICLES).document(vehicleId)
            .collection(MAINTENANCE)

    private fun recordsFlow(collection: CollectionReference, vehicleId: String): Flow<List<MaintenanceRecord>> = callbackFlow {
        val registration = collection
            .orderBy(FIELD_DATE, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    closeWithListenerError(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents.orEmpty().mapNotNull { it.toRecord(vehicleId) })
            }
        awaitClose { registration.remove() }
    }

    private fun recordFlow(collection: CollectionReference, vehicleId: String, recordId: String): Flow<MaintenanceRecord?> = callbackFlow {
        val registration = collection.document(recordId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                closeWithListenerError(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.takeIf { it.exists() }?.toRecord(vehicleId))
        }
        awaitClose { registration.remove() }
    }

    private fun MaintenanceRecord.toMap(): Map<String, Any?> = mapOf(
        FIELD_DATE to date,
        "mileage" to mileage,
        "workshop" to workshop,
        "totalCost" to totalCost,
        "notes" to notes,
        "items" to items.map { it.toMap() },
        FIELD_TYPE to type.name,
        // Kept so older app versions still tell upgrades apart.
        FIELD_IS_UPGRADE to (type == RecordType.Upgrade),
        FIELD_HAS_RECEIPT to hasReceipt,
    )

    private fun MaintenanceItem.toMap(): Map<String, Any?> = mapOf(
        "service" to service,
        "cost" to cost,
        "laborCost" to laborCost,
        "notes" to notes,
    )

    private fun DocumentSnapshot.toRecord(vehicleId: String): MaintenanceRecord? {
        val rawItems = get("items") as? List<*> ?: emptyList<Any>()
        return MaintenanceRecord(
            id = id,
            vehicleId = vehicleId,
            date = getString(FIELD_DATE) ?: return null,
            mileage = getLong("mileage")?.toInt() ?: 0,
            workshop = getString("workshop").orEmpty(),
            totalCost = getDouble("totalCost"),
            notes = getString("notes").orEmpty(),
            items = rawItems.mapNotNull { (it as? Map<*, *>)?.toItem() },
            type = RecordType.fromName(getString(FIELD_TYPE))
                ?: if (getBoolean(FIELD_IS_UPGRADE) == true) RecordType.Upgrade else RecordType.Maintenance,
            hasReceipt = getBoolean(FIELD_HAS_RECEIPT) ?: false,
        )
    }

    private fun Map<*, *>.toItem(): MaintenanceItem = MaintenanceItem(
        service = this["service"] as? String ?: this["category"] as? String ?: "",
        cost = (this["cost"] as? Number)?.toDouble() ?: 0.0,
        laborCost = (this["laborCost"] as? Number)?.toDouble() ?: 0.0,
        notes = this["notes"] as? String ?: "",
    )

    private companion object {
        const val USERS = "users"
        const val VEHICLES = "vehicles"
        const val MAINTENANCE = "maintenance"
        const val FIELD_DATE = "date"
        const val FIELD_TYPE = "type"
        const val FIELD_IS_UPGRADE = "isUpgrade"
        const val FIELD_HAS_RECEIPT = "hasReceipt"
        const val FIELD_DATA = "data"
        const val ATTACHMENTS = "attachments"
        const val RECEIPT = "receipt"
        const val FIELD_CREATED_AT = "createdAt"
    }
}
