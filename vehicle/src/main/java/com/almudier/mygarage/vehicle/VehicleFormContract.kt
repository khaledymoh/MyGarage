package com.almudier.mygarage.vehicle

import com.almudier.mygarage.core.presentation.mvi.UiEffect
import com.almudier.mygarage.core.presentation.mvi.UiIntent
import com.almudier.mygarage.core.presentation.mvi.UiState

object VehicleFormContract {

    data class State(
        val editing: Boolean = false,
        val vehicleId: String? = null,
        val manufacturer: String = "",
        val model: String = "",
        val year: String = "",
        val trim: String = "",
        val mileage: String = "",
        val plate: String = "",
        val vin: String = "",
        val color: String = "",
        val photoUri: String? = null,
        val minMileage: Int = 0,
        val isSaving: Boolean = false,
        val isLoading: Boolean = false,
    ) : UiState {
        val canSave: Boolean
            get() = !isLoading &&
                    manufacturer.isNotBlank() &&
                    model.isNotBlank() &&
                    year.toIntOrNull() != null &&
                    mileage.toIntOrNull() != null &&
                    !isMileageBelowLatestService

        val isMileageBelowLatestService: Boolean
            get() = mileage.toIntOrNull()?.let { it < minMileage } ?: false
    }

    sealed interface Intent : UiIntent {
        data class Load(val id: String?) : Intent
        data class Update(val field: String, val value: String) : Intent
        data object Back : Intent
        data object Save : Intent
    }

    sealed interface Effect : UiEffect
}
