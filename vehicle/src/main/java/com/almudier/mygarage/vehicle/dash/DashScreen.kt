package com.almudier.mygarage.vehicle.dash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.almudier.mygarage.core.domain.model.DefaultServiceIntervals
import com.almudier.mygarage.core.domain.model.MaintenanceItem
import com.almudier.mygarage.core.domain.model.MaintenanceRecord
import com.almudier.mygarage.core.domain.model.RecordType
import com.almudier.mygarage.core.domain.model.ReminderStatus
import com.almudier.mygarage.core.domain.model.ServiceInterval
import com.almudier.mygarage.core.domain.model.Vehicle
import com.almudier.mygarage.core.domain.model.firstRecordDate
import com.almudier.mygarage.core.domain.model.monthlySpend
import com.almudier.mygarage.core.domain.model.reminderStatuses
import com.almudier.mygarage.core.domain.model.totalsByType
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.components.BarSegment
import com.almudier.mygarage.core.presentation.components.GarageBarChart
import com.almudier.mygarage.core.presentation.components.GarageButton
import com.almudier.mygarage.core.presentation.components.GarageCard
import com.almudier.mygarage.core.presentation.components.GarageDot
import com.almudier.mygarage.core.presentation.components.GarageEmptyState
import com.almudier.mygarage.core.presentation.components.GarageListGroup
import com.almudier.mygarage.core.presentation.components.GarageProgressBar
import com.almudier.mygarage.core.presentation.components.GarageSectionHeader
import com.almudier.mygarage.core.presentation.components.GarageStackedBar
import com.almudier.mygarage.core.presentation.components.LocalMainBottomBarInset
import com.almudier.mygarage.core.presentation.shared.Amount
import com.almudier.mygarage.core.presentation.shared.AmountSize
import com.almudier.mygarage.core.presentation.shared.RecordRow
import com.almudier.mygarage.core.presentation.shared.color
import com.almudier.mygarage.core.presentation.shared.pluralLabel
import com.almudier.mygarage.core.presentation.shared.reminderColor
import com.almudier.mygarage.core.presentation.shared.reminderDueText
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme
import com.almudier.mygarage.vehicle.R
import com.almudier.mygarage.vehicle.api.DashRoute
import com.almudier.mygarage.vehicle.common.CarHeader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DashScreen(route: DashRoute, viewModel: DashViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(route.vehicleId) { viewModel.onEvent(DashContract.Intent.Load(route.vehicleId)) }
    DashScreenContent(
        vehicle = state.vehicle,
        records = state.records,
        intervals = state.intervals,
        isLoading = state.isLoading,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun DashScreenContent(
    vehicle: Vehicle,
    records: List<MaintenanceRecord>,
    intervals: List<ServiceInterval>,
    isLoading: Boolean,
    onEvent: (DashContract.Intent) -> Unit,
    today: LocalDate = remember { LocalDate.now() },
) {
    val spacing = GarageDesignTheme.spacing
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GarageDesignTheme.colors.primary)
        }
        return
    }
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.nr)
            .padding(bottom = LocalMainBottomBarInset.current + spacing.xl),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        CarHeader(
            vehicle = vehicle,
            onOpenProfile = { onEvent(DashContract.Intent.OpenProfile) },
            onSwitchCar = { onEvent(DashContract.Intent.SwitchCar) },
        )
        val months = remember(records, today) { monthlySpend(records, 7, today) }
        SpendCard(records, months, today)
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            ThisMonthTile(months, Modifier.weight(1f))
            LastWashTile(records, today, Modifier.weight(1f))
        }
        if (records.isEmpty()) {
            GarageCard(Modifier.fillMaxWidth()) {
                GarageEmptyState(
                    icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                    title = stringResource(R.string.dash_empty_title),
                    text = stringResource(R.string.dash_empty_text),
                    action = { GarageButton(stringResource(R.string.dash_empty_action), onClick = { onEvent(DashContract.Intent.AddRecord) }) },
                )
            }
        } else {
            val reminders = remember(records, intervals, today) { reminderStatuses(intervals, records, today).take(3) }
            if (reminders.isNotEmpty()) RemindersCard(reminders)
            GarageSectionHeader(stringResource(R.string.dash_latest), Modifier.padding(top = spacing.xs))
            GarageListGroup(Modifier.fillMaxWidth()) {
                records.take(3).forEach { record ->
                    RecordRow(record, onClick = { onEvent(DashContract.Intent.OpenRecord(record.id)) })
                }
            }
        }
    }
}

private val monthYear = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)

/** Total spent, all time, split by type; then spend by month. */
@Composable
private fun SpendCard(records: List<MaintenanceRecord>, months: List<com.almudier.mygarage.core.domain.model.MonthSpend>, today: LocalDate) {
    val totals = remember(records) { totalsByType(records) }
    val total = totals.values.sum()
    GarageCard(Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
        GarageSectionHeader(stringResource(R.string.dash_total_title))
        Amount(total, size = AmountSize.Hero, modifier = Modifier.padding(top = 2.dp))
        val since = firstRecordDate(records)
        Text(
            if (since == null) stringResource(R.string.dash_records_none)
            else pluralStringResource(R.plurals.dash_total_caption, records.size, records.size, since.format(monthYear)),
            style = GarageDesignTheme.typography.caption.copy(color = GarageDesignTheme.colors.primary, fontSize = 11.5.sp),
        )
        GarageStackedBar(
            RecordType.entries.map { BarSegment(totals.getValue(it).toFloat(), it.color) },
            Modifier.padding(top = 12.dp, bottom = 8.dp),
        )
        RecordType.entries.forEach { type ->
            Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                GarageDot(type.color, size = 8.dp)
                Text(
                    stringResource(type.pluralLabel),
                    style = GarageDesignTheme.typography.body.copy(color = GarageDesignTheme.colors.dim),
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                )
                Amount(totals.getValue(type), size = AmountSize.Small)
            }
        }
        GarageSectionHeader(stringResource(R.string.dash_by_month), Modifier.padding(top = 12.dp))
        GarageBarChart(
            columns = months.map { month -> listOf(RecordType.Detailing, RecordType.Maintenance, RecordType.Upgrade).map { BarSegment(month.byType.getValue(it).toFloat(), it.color) } },
            labels = months.map { it.month.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase() },
            highlightIndex = months.lastIndex,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun ThisMonthTile(months: List<com.almudier.mygarage.core.domain.model.MonthSpend>, modifier: Modifier) {
    val current = months.lastOrNull()?.total ?: 0.0
    val previous = months.getOrNull(months.lastIndex - 1)
    val previousName = previous?.month?.month?.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).orEmpty()
    val caption = when {
        previous == null || previous.total == 0.0 -> stringResource(R.string.dash_vs_last_none, previousName)
        else -> {
            val change = ((current - previous.total) / previous.total * 100).roundToInt()
            if (change <= 0) stringResource(R.string.dash_vs_last_less, abs(change), previousName)
            else stringResource(R.string.dash_vs_last_more, change, previousName)
        }
    }
    StatTile(stringResource(R.string.dash_this_month), caption, modifier) { Amount(current, size = AmountSize.Large) }
}

@Composable
private fun LastWashTile(records: List<MaintenanceRecord>, today: LocalDate, modifier: Modifier) {
    val washes = remember(records) {
        records.filter { it.type == RecordType.Detailing }
            .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
            .sorted()
    }
    val last = washes.lastOrNull()
    val avgGap = if (washes.size >= 2) ChronoUnit.DAYS.between(washes.first(), washes.last()) / (washes.size - 1) else null
    StatTile(
        label = stringResource(R.string.dash_last_wash),
        caption = when {
            last == null -> stringResource(R.string.dash_last_wash_none)
            avgGap != null -> stringResource(R.string.dash_last_wash_avg, avgGap.toInt())
            else -> null
        },
        modifier = modifier,
    ) {
        Text(
            if (last == null) "—" else stringResource(R.string.dash_last_wash_value, ChronoUnit.DAYS.between(last, today).toInt()),
            style = GarageDesignTheme.typography.number.copy(color = if (last == null) GarageDesignTheme.colors.mute else GarageDesignTheme.colors.text),
        )
    }
}

/** A small number tile: label, value slot, caption. */
@Composable
private fun StatTile(label: String, caption: String?, modifier: Modifier = Modifier, value: @Composable () -> Unit) {
    GarageCard(modifier) {
        GarageSectionHeader(label)
        Spacer(Modifier.height(4.dp))
        value()
        if (caption != null) Text(caption, style = GarageDesignTheme.typography.caption, maxLines = 2)
    }
}

/** Time-based service reminders, most urgent first. No odometer needed. */
@Composable
private fun RemindersCard(reminders: List<ReminderStatus>) {
    GarageCard(Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
        GarageSectionHeader(stringResource(R.string.dash_reminders), trailing = { Text(stringResource(R.string.dash_reminders_by_time)) })
        reminders.forEach { status ->
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(status.interval.name, style = GarageDesignTheme.typography.label, modifier = Modifier.weight(1f))
                Text(reminderDueText(status), style = GarageDesignTheme.typography.mono.copy(color = reminderColor(status.state)))
            }
            GarageProgressBar(status.elapsed, reminderColor(status.state), Modifier.padding(top = 7.dp))
        }
    }
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun DashScreenPreview() {
    val records = listOf(
        MaintenanceRecord("1", "v", "2026-10-15", 0, "Tune Lab", 60.0, items = listOf(MaintenanceItem("Resonator install")), type = RecordType.Upgrade),
        MaintenanceRecord("2", "v", "2026-10-12", 0, "", 5.0, items = listOf(MaintenanceItem("Car wash")), type = RecordType.Detailing),
        MaintenanceRecord("3", "v", "2026-10-12", 48600, "Bavaria Motors", 35.0, items = listOf(MaintenanceItem("Brake fluid"))),
        MaintenanceRecord("4", "v", "2026-09-22", 0, "Tune Lab", 1480.0, items = listOf(MaintenanceItem("Body kit")), type = RecordType.Upgrade),
        MaintenanceRecord("5", "v", "2026-09-06", 0, "", 5.0, items = listOf(MaintenanceItem("Car wash")), type = RecordType.Detailing),
        MaintenanceRecord("6", "v", "2026-05-06", 47000, "Bavaria Motors", 68.0, items = listOf(MaintenanceItem("Oil change"))),
    )
    DashScreenContent(
        vehicle = Vehicle("v", "BMW", "330e", 2020, "M Sport", 48600, "21-88330", color = "Grey"),
        records = records,
        intervals = DefaultServiceIntervals,
        isLoading = false,
        onEvent = {},
        today = LocalDate.of(2026, 10, 16),
    )
}
