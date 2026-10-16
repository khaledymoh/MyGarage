package com.almudier.mygarage.core.domain.repository

import com.almudier.mygarage.core.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun observeVehicles(): Flow<List<Vehicle>>
    fun observeVehicle(id: String): Flow<Vehicle?>
    suspend fun deleteVehicle(id: String): Result<Unit>
    suspend fun saveVehicle(vehicle: Vehicle): Result<Vehicle>
}
