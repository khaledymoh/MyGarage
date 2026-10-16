package com.almudier.mygarage.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class RemindersTest {

    private val oil = ServiceInterval("engine_oil", "Engine oil", 6, listOf("oil change", "engine oil"))
    private val today = LocalDate.of(2026, 10, 16)

    private fun record(date: String, service: String, type: RecordType = RecordType.Maintenance) =
        MaintenanceRecord(id = date, vehicleId = "v", date = date, mileage = 0, workshop = "", items = listOf(MaintenanceItem(service)), type = type)

    @Test
    fun `no matching record means NoRecord`() {
        val status = reminderStatus(oil, listOf(record("2026-05-01", "Car wash")), today)
        assertEquals(ReminderState.NoRecord, status.state)
        assertNull(status.dueDate)
    }

    @Test
    fun `due date is last matching record plus interval`() {
        val status = reminderStatus(oil, listOf(record("2026-01-10", "Oil change"), record("2026-07-01", "Engine oil + filter")), today)
        assertEquals(LocalDate.of(2026, 7, 1), status.lastDone)
        assertEquals(LocalDate.of(2027, 1, 1), status.dueDate)
        assertEquals(ReminderState.OnTrack, status.state)
    }

    @Test
    fun `within 28 days is DueSoon`() {
        val status = reminderStatus(oil, listOf(record("2026-05-06", "oil change")), today)
        assertEquals(LocalDate.of(2026, 11, 6), status.dueDate)
        assertEquals(21L, status.daysLeft)
        assertEquals(ReminderState.DueSoon, status.state)
    }

    @Test
    fun `past due is Overdue with full progress`() {
        val status = reminderStatus(oil, listOf(record("2026-03-01", "Oil change")), today)
        assertEquals(ReminderState.Overdue, status.state)
        assertEquals(1f, status.elapsed)
    }

    @Test
    fun `upgrades and detailing never count as a service`() {
        val status = reminderStatus(oil, listOf(record("2026-10-01", "Oil change", RecordType.Upgrade)), today)
        assertEquals(ReminderState.NoRecord, status.state)
    }

    @Test
    fun `statuses are ordered by urgency with never-logged last`() {
        val inspection = ServiceInterval("inspection", "Annual inspection", 12, listOf("inspection"))
        val fluid = ServiceInterval("brake_fluid", "Brake fluid", 24, listOf("brake fluid"))
        val ordered = reminderStatuses(
            listOf(fluid, inspection, oil),
            listOf(record("2026-05-06", "Oil change"), record("2026-03-01", "Inspection")),
            today,
        ).map { it.interval.id }
        assertEquals(listOf("engine_oil", "inspection", "brake_fluid"), ordered)
    }
}
