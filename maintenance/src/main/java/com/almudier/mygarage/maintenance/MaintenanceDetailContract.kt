package com.almudier.mygarage.maintenance

import com.almudier.mygarage.core.domain.model.EmptyMaintenanceRecord
import com.almudier.mygarage.core.domain.model.MaintenanceRecord
import com.almudier.mygarage.core.presentation.mvi.UiEffect
import com.almudier.mygarage.core.presentation.mvi.UiIntent
import com.almudier.mygarage.core.presentation.mvi.UiState

object MaintenanceDetailContract {

    data class State(
        val record: MaintenanceRecord = EmptyMaintenanceRecord,
        val showDelete: Boolean = false,
        /** Receipt image as a `data:` URI, loaded separately from the record. */
        val receipt: String? = null,
        val showReceipt: Boolean = false,
        val isDeleting: Boolean = false,
    ) : UiState

    sealed interface Intent : UiIntent {
        data class Load(val vehicleId: String, val id: String) : Intent
        data object Back : Intent
        data object Edit : Intent
        data object AskDelete : Intent
        data object CancelDelete : Intent
        data object ConfirmDelete : Intent
        data class ShowReceipt(val show: Boolean) : Intent
    }

    sealed interface Effect : UiEffect
}
