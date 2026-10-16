package com.almudier.mygarage.vehicle.log

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.almudier.mygarage.core.domain.model.MaintenanceItem
import com.almudier.mygarage.core.domain.model.MaintenanceRecord
import com.almudier.mygarage.core.domain.model.RecordType
import com.almudier.mygarage.core.domain.model.Vehicle
import com.almudier.mygarage.core.domain.model.cost
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.components.GarageBottomSheet
import com.almudier.mygarage.core.presentation.components.GarageButton
import com.almudier.mygarage.core.presentation.components.GarageButtonSize
import com.almudier.mygarage.core.presentation.components.GarageButtonStyle
import com.almudier.mygarage.core.presentation.components.GarageChip
import com.almudier.mygarage.core.presentation.components.GarageChipStyle
import com.almudier.mygarage.core.presentation.components.GarageDialogButton
import com.almudier.mygarage.core.presentation.components.GarageEmptyState
import com.almudier.mygarage.core.presentation.components.GarageIconButton
import com.almudier.mygarage.core.presentation.components.GarageIconButtonStyle
import com.almudier.mygarage.core.presentation.components.GarageSectionHeader
import com.almudier.mygarage.core.presentation.components.GarageTextField
import com.almudier.mygarage.core.presentation.components.GarageTextFieldStyle
import com.almudier.mygarage.core.presentation.components.LocalMainBottomBarInset
import com.almudier.mygarage.core.presentation.shared.LocalCurrency
import com.almudier.mygarage.core.presentation.shared.RecordRow
import com.almudier.mygarage.core.presentation.shared.color
import com.almudier.mygarage.core.presentation.shared.formatMoney
import com.almudier.mygarage.core.presentation.shared.pluralLabel
import com.almudier.mygarage.core.presentation.shared.tabLabel
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme
import com.almudier.mygarage.vehicle.R
import com.almudier.mygarage.vehicle.api.LogRoute
import com.almudier.mygarage.vehicle.common.CarHeader
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun LogScreen(route: LogRoute, viewModel: LogViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(route.vehicleId) { viewModel.onEvent(LogContract.Intent.Load(route.vehicleId)) }
    LogScreenContent(
        vehicle = state.vehicle,
        records = state.records,
        filters = state.filters,
        draft = state.draft,
        isLoading = state.isLoading,
        onEvent = viewModel::onEvent,
    )
}

private val monthTitle = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogScreenContent(
    vehicle: Vehicle,
    records: List<MaintenanceRecord>,
    filters: LogFilters,
    draft: LogFilters?,
    isLoading: Boolean,
    onEvent: (LogContract.Intent) -> Unit,
    today: LocalDate = remember { LocalDate.now() },
) {
    val spacing = GarageDesignTheme.spacing
    val colors = GarageDesignTheme.colors
    val currency = LocalCurrency.current
    val results = remember(records, filters, today) { filters.apply(records, today) }
    val byMonth = remember(results) {
        results.groupBy { runCatching { YearMonth.from(LocalDate.parse(it.date)) }.getOrNull() }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Column(Modifier.padding(horizontal = spacing.nr)) {
            CarHeader(vehicle, onOpenProfile = { onEvent(LogContract.Intent.OpenProfile) }, onSwitchCar = { onEvent(LogContract.Intent.SwitchCar) })
            GarageTextField(
                value = filters.query,
                onValueChange = { onEvent(LogContract.Intent.Query(it)) },
                style = GarageTextFieldStyle.Search,
                placeholder = stringResource(R.string.log_search_placeholder),
                leading = { Icon(Icons.Rounded.Search, contentDescription = null, Modifier.size(18.dp)) },
                trailing = {
                    if (filters.query.isNotEmpty()) {
                        GarageIconButton(Icons.Rounded.Close, stringResource(R.string.log_clear_search), onClick = { onEvent(LogContract.Intent.Query("")) }, style = GarageIconButtonStyle.Ghost)
                    } else {
                        GarageIconButton(Icons.Rounded.Tune, stringResource(R.string.log_filters), onClick = { onEvent(LogContract.Intent.OpenFilters) }, style = GarageIconButtonStyle.Ghost)
                    }
                },
                modifier = Modifier.padding(top = spacing.xs),
            )
        }
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = spacing.nr, vertical = spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            GarageChip(stringResource(R.string.log_all, records.size), selected = filters.types.isEmpty(), onClick = { onEvent(LogContract.Intent.QuickType(null)) })
            RecordType.entries.forEach { type ->
                GarageChip(
                    text = stringResource(type.pluralLabel),
                    selected = filters.types == setOf(type),
                    onClick = { onEvent(LogContract.Intent.QuickType(type)) },
                    dot = type.color,
                )
            }
            GarageChip(stringResource(R.string.log_filters), selected = false, onClick = { onEvent(LogContract.Intent.OpenFilters) }, icon = Icons.Rounded.Tune)
        }

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = colors.primary) }
            records.isEmpty() -> GarageEmptyState(
                icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                title = stringResource(R.string.log_empty_car_title),
                text = stringResource(R.string.log_empty_car_text),
                modifier = Modifier.padding(top = spacing.xl),
            )
            results.isEmpty() -> GarageEmptyState(
                icon = Icons.Rounded.SearchOff,
                title = stringResource(R.string.log_empty_title),
                text = if (filters.query.isNotBlank()) stringResource(R.string.log_empty_search, filters.query.trim()) else stringResource(R.string.log_empty_filters),
                action = { GarageButton(stringResource(R.string.log_clear_filters), onClick = { onEvent(LogContract.Intent.ClearFilters) }, style = GarageButtonStyle.Secondary, size = GarageButtonSize.Small) },
                modifier = Modifier.padding(top = spacing.xl),
            )
            else -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = spacing.nr, end = spacing.nr, bottom = LocalMainBottomBarInset.current + spacing.xl),
            ) {
                if (filters.isActive) {
                    item {
                        Text(
                            pluralStringResource(R.plurals.log_results, results.size, results.size, formatMoney(results.sumOf { it.cost }, currency)),
                            style = GarageDesignTheme.typography.mono.copy(color = colors.dim),
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                }
                byMonth.forEach { (month, monthRecords) ->
                    stickyHeader(key = "h-$month") {
                        GarageSectionHeader(
                            title = stringResource(
                                R.string.log_month_total,
                                month?.format(monthTitle) ?: "—",
                                formatMoney(monthRecords.sumOf { it.cost }, currency),
                            ),
                            modifier = Modifier.background(colors.background).padding(top = 14.dp, bottom = 6.dp),
                        )
                    }
                    items(monthRecords, key = { it.id }) { record ->
                        Column {
                            RecordRow(record, onClick = { onEvent(LogContract.Intent.OpenRecord(record.id)) }, highlight = filters.query)
                            Box(Modifier.fillMaxWidth().height(GarageDesignTheme.stroke.thin).background(colors.line))
                        }
                    }
                }
            }
        }
    }

    if (draft != null) {
        FilterSheet(vehicle, records, draft, today, onEvent)
    }
}

/** Every filter today's search offers (service, dates, mileage) plus record type. No vehicle filter. */
@Composable
private fun FilterSheet(
    vehicle: Vehicle,
    records: List<MaintenanceRecord>,
    draft: LogFilters,
    today: LocalDate,
    onEvent: (LogContract.Intent) -> Unit,
) {
    val spacing = GarageDesignTheme.spacing
    val count = remember(records, draft, today) { draft.apply(records, today).size }
    fun edit(change: LogFilters.() -> LogFilters) = onEvent(LogContract.Intent.EditDraft(draft.change()))
    GarageBottomSheet(
        onDismissRequest = { onEvent(LogContract.Intent.CloseFilters) },
        title = { Text(stringResource(R.string.log_filters_title, "${vehicle.manufacturer} ${vehicle.model}")) },
        titleAction = { GarageDialogButton(stringResource(R.string.log_reset), onClick = { onEvent(LogContract.Intent.EditDraft(LogFilters(query = draft.query))) }) },
    ) {
        GarageSectionHeader(stringResource(R.string.log_type))
        FlowRow(Modifier.padding(top = spacing.xs, bottom = spacing.nr), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RecordType.entries.forEach { type ->
                GarageChip(
                    text = stringResource(type.tabLabel),
                    selected = type in draft.types,
                    onClick = { edit { copy(types = if (type in types) types - type else types + type) } },
                    style = GarageChipStyle.Tinted(type.color),
                )
            }
        }
        GarageTextField(
            value = draft.service,
            onValueChange = { edit { copy(service = it) } },
            label = stringResource(R.string.log_service),
            placeholder = stringResource(R.string.log_service_hint),
        )
        GarageSectionHeader(stringResource(R.string.log_when), Modifier.padding(top = spacing.nr))
        FlowRow(Modifier.padding(top = spacing.xs, bottom = spacing.nr), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                DateRange.Any to R.string.log_when_any,
                DateRange.Last30Days to R.string.log_when_30,
                DateRange.Last3Months to R.string.log_when_90,
                DateRange.ThisYear to R.string.log_when_year,
            ).forEach { (range, label) ->
                GarageChip(stringResource(label), selected = draft.dateRange == range, onClick = { edit { copy(dateRange = range) } })
            }
        }
        GarageSectionHeader(stringResource(R.string.log_mileage))
        Row(Modifier.padding(top = spacing.xs), horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
            GarageTextField(
                value = draft.minKm, onValueChange = { v -> edit { copy(minKm = v.filter(Char::isDigit)) } },
                label = stringResource(R.string.log_min_km), placeholder = "0",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            GarageTextField(
                value = draft.maxKm, onValueChange = { v -> edit { copy(maxKm = v.filter(Char::isDigit)) } },
                label = stringResource(R.string.log_max_km), placeholder = "—",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(spacing.nr))
        GarageButton(pluralStringResource(R.plurals.log_show_results, count, count), onClick = { onEvent(LogContract.Intent.ApplyDraft) }, modifier = Modifier.fillMaxWidth())
    }
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun LogScreenPreview() {
    val records = listOf(
        MaintenanceRecord("1", "v", "2026-10-15", 0, "Tune Lab", 60.0, items = listOf(MaintenanceItem("Resonator install")), type = RecordType.Upgrade),
        MaintenanceRecord("2", "v", "2026-10-12", 48600, "Bavaria Motors", 35.0, items = listOf(MaintenanceItem("Brake fluid"))),
        MaintenanceRecord("3", "v", "2026-09-27", 0, "", 5.0, items = listOf(MaintenanceItem("Car wash")), type = RecordType.Detailing),
    )
    LogScreenContent(
        vehicle = Vehicle("v", "BMW", "330e", 2020, "M Sport", 48600, "", color = ""),
        records = records,
        filters = LogFilters(),
        draft = null,
        isLoading = false,
        onEvent = {},
        today = LocalDate.of(2026, 10, 16),
    )
}
