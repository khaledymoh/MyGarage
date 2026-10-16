package com.almudier.mygarage.core.domain.repository

import com.almudier.mygarage.core.domain.model.ServiceInterval
import kotlinx.coroutines.flow.Flow

/** Device-level app settings: the car the user picked, currency, and service intervals. */
interface AppSettingsRepository {
    val selectedVehicleId: Flow<String?>
    val currency: Flow<String>
    val serviceIntervals: Flow<List<ServiceInterval>>

    suspend fun selectVehicle(id: String?)
    suspend fun setCurrency(code: String)
    suspend fun setIntervalMonths(id: String, months: Int)
}
