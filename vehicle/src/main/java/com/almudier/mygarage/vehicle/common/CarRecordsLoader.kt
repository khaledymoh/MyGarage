package com.almudier.mygarage.vehicle.common

import com.almudier.mygarage.core.domain.model.MaintenanceRecord
import com.almudier.mygarage.core.domain.model.Vehicle
import com.almudier.mygarage.core.domain.model.withServiceMileage
import com.almudier.mygarage.core.domain.repository.AppSettingsRepository
import com.almudier.mygarage.core.domain.repository.MaintenanceRepository
import com.almudier.mygarage.core.domain.repository.VehicleRepository
import com.almudier.mygarage.core.presentation.navigation.Navigator
import com.almudier.mygarage.garage.api.GarageRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** The picked car with its records, as one stream for Dash, Log and Insights. */
data class CarData(val vehicle: Vehicle, val records: List<MaintenanceRecord>)

class CarRecordsLoader @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val appSettings: AppSettingsRepository,
    private val navigator: Navigator,
) {
    fun observe(vehicleId: String): Flow<CarData?> =
        combine(vehicleRepository.observeVehicle(vehicleId), maintenanceRepository.observeRecords(vehicleId)) { vehicle, records ->
            vehicle?.let { CarData(it.withServiceMileage(records), records) }
        }

    /** Goes back to the garage to pick another car. */
    fun switchCar() = navigator.navigate(GarageRoute)

    /** The picked car no longer exists (deleted elsewhere): forget it and show the garage. */
    suspend fun onCarMissing() {
        appSettings.selectVehicle(null)
        navigator.replaceAll(GarageRoute)
    }
}
