package com.almudier.mygarage.vehicle.dash

import com.almudier.mygarage.core.domain.model.EmptyVehicle
import com.almudier.mygarage.core.domain.model.MaintenanceRecord
import com.almudier.mygarage.core.domain.model.ServiceInterval
import com.almudier.mygarage.core.domain.model.Vehicle
import com.almudier.mygarage.core.presentation.mvi.UiEffect
import com.almudier.mygarage.core.presentation.mvi.UiIntent
import com.almudier.mygarage.core.presentation.mvi.UiState

object DashContract {

    data class State(
        val vehicle: Vehicle = EmptyVehicle,
        val records: List<MaintenanceRecord> = emptyList(),
        val intervals: List<ServiceInterval> = emptyList(),
        val isLoading: Boolean = true,
    ) : UiState

    sealed interface Intent : UiIntent {
        data class Load(val vehicleId: String) : Intent
        data object OpenProfile : Intent
        data object SwitchCar : Intent
        data object AddRecord : Intent
        data class OpenRecord(val id: String) : Intent
    }

    sealed interface Effect : UiEffect
}
