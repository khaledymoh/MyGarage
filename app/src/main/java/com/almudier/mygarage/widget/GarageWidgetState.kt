package com.almudier.mygarage.widget

import android.content.Context
import com.almudier.mygarage.core.domain.model.DefaultServiceIntervals
import com.almudier.mygarage.core.domain.model.RecordType
import com.almudier.mygarage.core.domain.model.ReminderStatus
import com.almudier.mygarage.core.domain.model.cost
import com.almudier.mygarage.core.domain.model.firstRecordDate
import com.almudier.mygarage.core.domain.model.reminderStatuses
import com.almudier.mygarage.core.domain.model.totalsByType
import com.almudier.mygarage.core.domain.repository.AppSettingsRepository
import com.almudier.mygarage.core.domain.repository.MaintenanceRepository
import com.almudier.mygarage.core.domain.repository.VehicleRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import java.time.YearMonth

/** What the home-screen widget shows. */
sealed interface GarageWidgetState {
    /** No car picked yet, not signed in, or the data could not be loaded. */
    data object NoCar : GarageWidgetState

    data class Ready(
        val carName: String,
        val currency: String,
        val total: Double,
        val byType: Map<RecordType, Double>,
        val thisMonth: Double,
        val since: LocalDate?,
        /** Most urgent first; services never logged come last. */
        val reminders: List<ReminderStatus>,
    ) : GarageWidgetState
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface GarageWidgetEntryPoint {
    fun vehicleRepository(): VehicleRepository
    fun maintenanceRepository(): MaintenanceRepository
    fun appSettingsRepository(): AppSettingsRepository
}

/** Loads the picked car's spend and reminders once, for a widget update. */
suspend fun loadGarageWidgetState(context: Context, today: LocalDate = LocalDate.now()): GarageWidgetState {
    val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, GarageWidgetEntryPoint::class.java)
    val settings = entryPoint.appSettingsRepository()
    return runCatching {
        withTimeoutOrNull(LOAD_TIMEOUT_MS) {
            val vehicleId = settings.selectedVehicleId.first() ?: return@withTimeoutOrNull GarageWidgetState.NoCar
            val vehicle = entryPoint.vehicleRepository().observeVehicle(vehicleId).first()
                ?: return@withTimeoutOrNull GarageWidgetState.NoCar
            val records = entryPoint.maintenanceRepository().observeRecords(vehicleId).first()
            val intervals = settings.serviceIntervals.first().ifEmpty { DefaultServiceIntervals }
            val month = YearMonth.from(today)
            GarageWidgetState.Ready(
                carName = "${vehicle.manufacturer} ${vehicle.model}".trim(),
                currency = settings.currency.first(),
                total = records.sumOf { it.cost },
                byType = totalsByType(records),
                thisMonth = records
                    .filter { runCatching { YearMonth.from(LocalDate.parse(it.date)) == month }.getOrDefault(false) }
                    .sumOf { it.cost },
                since = firstRecordDate(records),
                reminders = reminderStatuses(intervals, records, today),
            )
        } ?: GarageWidgetState.NoCar
    }.getOrDefault(GarageWidgetState.NoCar)
}

private const val LOAD_TIMEOUT_MS = 20_000L
