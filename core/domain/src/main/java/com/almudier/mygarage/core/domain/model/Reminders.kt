package com.almudier.mygarage.core.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * A time-based service interval: "engine oil every 6 months". A record counts as this service
 * when any of its items' names contains one of [keywords] (case-insensitive).
 */
data class ServiceInterval(
    val id: String,
    val name: String,
    val months: Int,
    val keywords: List<String>,
)

val DefaultServiceIntervals: List<ServiceInterval> = listOf(
    ServiceInterval("engine_oil", "Engine oil", 6, listOf("oil change", "engine oil", "oil service", "oil filter")),
    ServiceInterval("inspection", "Annual inspection", 12, listOf("inspection")),
    ServiceInterval("tire_rotation", "Tire rotation", 6, listOf("tire rotation", "tyre rotation")),
    ServiceInterval("air_filter", "Air filter", 12, listOf("air filter")),
    ServiceInterval("brake_fluid", "Brake fluid", 24, listOf("brake fluid", "brake oil", "breake oil")),
)

enum class ReminderState { OnTrack, DueSoon, Overdue, NoRecord }

data class ReminderStatus(
    val interval: ServiceInterval,
    val lastDone: LocalDate?,
    val dueDate: LocalDate?,
    /** Days until due; negative when overdue; null without a matching record. */
    val daysLeft: Long?,
    /** Share of the interval that has passed, 0..1 (1 when overdue). */
    val elapsed: Float,
    val state: ReminderState,
)

const val DUE_SOON_DAYS = 28

/** Works out a reminder from the car's maintenance records. Only time matters; mileage is ignored. */
fun reminderStatus(interval: ServiceInterval, records: List<MaintenanceRecord>, today: LocalDate): ReminderStatus {
    val lastDone = records
        .asSequence()
        .filter { it.type == RecordType.Maintenance && it.matches(interval) }
        .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
        .maxOrNull()
        ?: return ReminderStatus(interval, null, null, null, 0f, ReminderState.NoRecord)
    val due = lastDone.plusMonths(interval.months.toLong())
    val daysLeft = ChronoUnit.DAYS.between(today, due)
    val total = ChronoUnit.DAYS.between(lastDone, due).coerceAtLeast(1)
    val elapsed = (ChronoUnit.DAYS.between(lastDone, today).toFloat() / total).coerceIn(0f, 1f)
    val state = when {
        daysLeft < 0 -> ReminderState.Overdue
        daysLeft <= DUE_SOON_DAYS -> ReminderState.DueSoon
        else -> ReminderState.OnTrack
    }
    return ReminderStatus(interval, lastDone, due, daysLeft, elapsed, state)
}

/** Reminders ordered by urgency: overdue, due soon, on track (soonest first), then never logged. */
fun reminderStatuses(intervals: List<ServiceInterval>, records: List<MaintenanceRecord>, today: LocalDate): List<ReminderStatus> =
    intervals.map { reminderStatus(it, records, today) }
        .sortedWith(compareBy({ it.state == ReminderState.NoRecord }, { it.daysLeft ?: Long.MAX_VALUE }))

private fun MaintenanceRecord.matches(interval: ServiceInterval): Boolean =
    items.any { item -> interval.keywords.any { item.service.contains(it, ignoreCase = true) } }
