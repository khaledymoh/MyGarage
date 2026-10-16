package com.almudier.mygarage.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class SpendingTest {

    private fun record(date: String, cost: Double?, type: RecordType) =
        MaintenanceRecord(id = date + cost, vehicleId = "v", date = date, mileage = 0, workshop = "", totalCost = cost, items = emptyList(), type = type)

    // The BMW 330e's real records (amounts are sample values).
    private val bmw = listOf(
        record("2026-10-15", 60.0, RecordType.Upgrade),
        record("2026-10-12", 5.0, RecordType.Detailing),
        record("2026-10-12", 35.0, RecordType.Maintenance),
        record("2026-10-12", 5.0, RecordType.Detailing),
        record("2026-10-12", 650.0, RecordType.Upgrade),
        record("2026-09-27", 5.0, RecordType.Detailing),
        record("2026-09-22", 1480.0, RecordType.Upgrade),
        record("2026-09-22", 40.0, RecordType.Upgrade),
        record("2026-09-15", 5.0, RecordType.Detailing),
        record("2026-09-06", 5.0, RecordType.Detailing),
    )

    @Test
    fun `totals by type cover every type`() {
        val totals = totalsByType(bmw)
        assertEquals(35.0, totals.getValue(RecordType.Maintenance), 0.0)
        assertEquals(2230.0, totals.getValue(RecordType.Upgrade), 0.0)
        assertEquals(25.0, totals.getValue(RecordType.Detailing), 0.0)
        assertEquals(0.0, totalsByType(emptyList()).getValue(RecordType.Upgrade), 0.0)
    }

    @Test
    fun `missing total counts as zero`() {
        assertEquals(0.0, totalsByType(listOf(record("2026-10-01", null, RecordType.Maintenance))).getValue(RecordType.Maintenance), 0.0)
    }

    @Test
    fun `monthly spend returns the last N months oldest first`() {
        val months = monthlySpend(bmw, 7, LocalDate.of(2026, 10, 16))
        assertEquals(7, months.size)
        assertEquals(YearMonth.of(2026, 4), months.first().month)
        assertEquals(YearMonth.of(2026, 10), months.last().month)
        assertEquals(755.0, months.last().total, 0.0)
        assertEquals(1535.0, months[5].total, 0.0)
        assertEquals(0.0, months[0].total, 0.0)
    }

    @Test
    fun `first record date is the earliest`() {
        assertEquals(LocalDate.of(2026, 9, 6), firstRecordDate(bmw))
    }
}
