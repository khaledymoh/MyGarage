package com.almudier.mygarage.maintenance

import com.almudier.mygarage.core.domain.model.RecordType
import com.almudier.mygarage.core.presentation.mvi.UiEffect
import com.almudier.mygarage.core.presentation.mvi.UiIntent
import com.almudier.mygarage.core.presentation.mvi.UiState

object MaintenanceFormContract {

    data class ItemForm(
        val service: String = "",
        val cost: String = "",
        val laborCost: String = "",
        val notes: String = "",
    ) {
        val isValid: Boolean
            get() = service.isNotBlank() && cost.toDoubleOrNull() != null &&
                (laborCost.isBlank() || laborCost.toDoubleOrNull() != null)

        val total: Double
            get() = (cost.toDoubleOrNull() ?: 0.0) + (laborCost.toDoubleOrNull() ?: 0.0)
    }

    data class State(
        val vehicleId: String = "",
        val recordId: String? = null,
        val date: String = "",
        val mileage: String = "",
        val workshop: String = "",
        val notes: String = "",
        val type: RecordType = RecordType.Maintenance,
        /** Receipt image: stored `data:` URI or a newly picked content URI; null = none. */
        val receipt: String? = null,
        val items: List<ItemForm> = listOf(ItemForm()),
        val services: List<String> = emptyList(),
        val isSaving: Boolean = false,
        val isLoading: Boolean = false,
    ) : UiState {
        val canSave: Boolean
            get() = !isLoading &&
                    vehicleId.isNotBlank() &&
                    date.isNotBlank() &&
                    (!type.tracksMileage || mileage.isBlank() || mileage.toIntOrNull() != null) &&
                    items.isNotEmpty() &&
                    items.all { it.isValid }

        val totalCost: Double
            get() = items.sumOf { it.total }
    }

    sealed interface Intent : UiIntent {
        data class Load(val vehicleId: String, val recordId: String?, val type: RecordType? = null) : Intent
        data class Update(val field: String, val value: String) : Intent
        data class UpdateItem(val index: Int, val field: String, val value: String) : Intent
        data class RemoveItem(val index: Int) : Intent
        data class SetType(val type: RecordType) : Intent
        data class SetReceipt(val uri: String?) : Intent
        /** Fills the first item with a preset service (detailing quick picks). */
        data class Preset(val service: String) : Intent
        data object AddItem : Intent
        data object Back : Intent
        data object Save : Intent
    }

    sealed interface Effect : UiEffect
}
