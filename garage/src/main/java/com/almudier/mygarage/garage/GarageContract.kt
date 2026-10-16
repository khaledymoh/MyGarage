package com.almudier.mygarage.garage

import com.almudier.mygarage.core.domain.model.MaintenanceRecord
import com.almudier.mygarage.core.domain.model.ServiceInterval
import com.almudier.mygarage.core.domain.model.Vehicle
import com.almudier.mygarage.core.presentation.mvi.UiEffect
import com.almudier.mygarage.core.presentation.mvi.UiIntent
import com.almudier.mygarage.core.presentation.mvi.UiState

object GarageContract {

    data class State(
        val vehicles: List<Vehicle> = emptyList(),
        val records: Map<String, List<MaintenanceRecord>> = emptyMap(),
        val intervals: List<ServiceInterval> = emptyList(),
        val selectedVehicleId: String? = null,
        val isLoading: Boolean = true,
    ) : UiState

    sealed interface Intent : UiIntent {
        data class OpenVehicle(val id: String) : Intent
        data object AddVehicle : Intent
        data object OpenSettings : Intent
    }

    sealed interface Effect : UiEffect
}
