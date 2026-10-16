package com.almudier.mygarage.core.domain.repository

import com.almudier.mygarage.core.domain.model.MaintenanceRecord
import kotlinx.coroutines.flow.Flow

interface MaintenanceRepository {
    fun observeRecords(vehicleId: String): Flow<List<MaintenanceRecord>>
    fun observeRecord(vehicleId: String, recordId: String): Flow<MaintenanceRecord?>
    suspend fun saveRecord(record: MaintenanceRecord): Result<MaintenanceRecord>
    suspend fun deleteRecord(vehicleId: String, recordId: String): Result<Unit>

    /** The record's receipt as a `data:` URI, or null when it has none. */
    suspend fun loadReceipt(vehicleId: String, recordId: String): Result<String?>
}
