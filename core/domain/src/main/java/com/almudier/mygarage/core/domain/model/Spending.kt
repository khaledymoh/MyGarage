package com.almudier.mygarage.core.domain.model

import java.time.LocalDate
import java.time.YearMonth

/** Total cost of a record; records without a total count as 0. */
val MaintenanceRecord.cost: Double get() = totalCost ?: 0.0

/** All-time spend per type. Every type is present, with 0 when it has no records. */
fun totalsByType(records: List<MaintenanceRecord>): Map<RecordType, Double> =
    RecordType.entries.associateWith { type -> records.filter { it.type == type }.sumOf { it.cost } }

data class MonthSpend(val month: YearMonth, val byType: Map<RecordType, Double>) {
    val total: Double get() = byType.values.sum()
}

/** Spend for the last [months] calendar months up to [today]'s month, oldest first. */
fun monthlySpend(records: List<MaintenanceRecord>, months: Int, today: LocalDate): List<MonthSpend> {
    val current = YearMonth.from(today)
    val byMonth = records.groupBy { record -> runCatching { YearMonth.from(LocalDate.parse(record.date)) }.getOrNull() }
    return (months - 1 downTo 0).map { back ->
        val month = current.minusMonths(back.toLong())
        MonthSpend(month, totalsByType(byMonth[month].orEmpty()))
    }
}

/** Earliest record date, for captions like "since Sep 2026". */
fun firstRecordDate(records: List<MaintenanceRecord>): LocalDate? =
    records.mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }.minOrNull()
