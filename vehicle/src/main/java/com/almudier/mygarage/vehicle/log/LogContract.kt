package com.almudier.mygarage.vehicle.log

import com.almudier.mygarage.core.domain.model.EmptyVehicle
import com.almudier.mygarage.core.domain.model.MaintenanceRecord
import com.almudier.mygarage.core.domain.model.RecordType
import com.almudier.mygarage.core.domain.model.Vehicle
import com.almudier.mygarage.core.presentation.mvi.UiEffect
import com.almudier.mygarage.core.presentation.mvi.UiIntent
import com.almudier.mygarage.core.presentation.mvi.UiState
import java.time.LocalDate

enum class DateRange { Any, Last30Days, Last3Months, ThisYear }

/** Filters for the selected car's Log. Search never spans other cars. */
data class LogFilters(
    val query: String = "",
    val types: Set<RecordType> = emptySet(),
    val service: String = "",
    val dateRange: DateRange = DateRange.Any,
    val minKm: String = "",
    val maxKm: String = "",
) {
    val isActive: Boolean
        get() = query.isNotBlank() || types.isNotEmpty() || service.isNotBlank() ||
            dateRange != DateRange.Any || minKm.isNotBlank() || maxKm.isNotBlank()

    fun apply(records: List<MaintenanceRecord>, today: LocalDate): List<MaintenanceRecord> {
        val from = when (dateRange) {
            DateRange.Any -> null
            DateRange.Last30Days -> today.minusDays(30)
            DateRange.Last3Months -> today.minusMonths(3)
            DateRange.ThisYear -> today.withDayOfYear(1)
        }
        val min = minKm.toIntOrNull()
        val max = maxKm.toIntOrNull()
        return records.filter { record ->
            (types.isEmpty() || record.type in types) &&
                record.matches(query) &&
                (service.isBlank() || record.items.any { it.service.contains(service.trim(), ignoreCase = true) }) &&
                (from == null || runCatching { !java.time.LocalDate.parse(record.date).isBefore(from) }.getOrDefault(true)) &&
                (min == null || record.mileage >= min) &&
                (max == null || (record.mileage in 1..max))
        }
    }
}

private fun MaintenanceRecord.matches(query: String): Boolean {
    val term = query.trim()
    if (term.isEmpty()) return true
    return workshop.contains(term, ignoreCase = true) ||
        notes.contains(term, ignoreCase = true) ||
        items.any { it.service.contains(term, ignoreCase = true) || it.notes.contains(term, ignoreCase = true) }
}

object LogContract {

    data class State(
        val vehicle: Vehicle = EmptyVehicle,
        val records: List<MaintenanceRecord> = emptyList(),
        val filters: LogFilters = LogFilters(),
        /** Draft edited in the filter sheet; applied with "Show results". */
        val draft: LogFilters? = null,
        val isLoading: Boolean = true,
    ) : UiState

    sealed interface Intent : UiIntent {
        data class Load(val vehicleId: String) : Intent
        data class Query(val value: String) : Intent
        /** Quick chip: null = all types. */
        data class QuickType(val type: RecordType?) : Intent
        data object OpenFilters : Intent
        data object CloseFilters : Intent
        data class EditDraft(val draft: LogFilters) : Intent
        data object ApplyDraft : Intent
        data object ClearFilters : Intent
        data class OpenRecord(val id: String) : Intent
        data object OpenProfile : Intent
        data object SwitchCar : Intent
    }

    sealed interface Effect : UiEffect
}
