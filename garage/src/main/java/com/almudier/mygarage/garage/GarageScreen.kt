package com.almudier.mygarage.garage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CarRepair
import androidx.compose.material.icons.rounded.LocalCarWash
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.almudier.mygarage.core.domain.model.DefaultServiceIntervals
import com.almudier.mygarage.core.domain.model.MaintenanceItem
import com.almudier.mygarage.core.domain.model.MaintenanceRecord
import com.almudier.mygarage.core.domain.model.RecordType
import com.almudier.mygarage.core.domain.model.ReminderState
import com.almudier.mygarage.core.domain.model.ReminderStatus
import com.almudier.mygarage.core.domain.model.ServiceInterval
import com.almudier.mygarage.core.domain.model.Vehicle
import com.almudier.mygarage.core.domain.model.cost
import com.almudier.mygarage.core.domain.model.reminderStatuses
import com.almudier.mygarage.core.domain.model.withServiceMileage
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.components.GarageBadge
import com.almudier.mygarage.core.presentation.components.GarageButton
import com.almudier.mygarage.core.presentation.components.GarageCard
import com.almudier.mygarage.core.presentation.components.GarageChevron
import com.almudier.mygarage.core.presentation.components.GarageIconButton
import com.almudier.mygarage.core.presentation.components.GarageListGroup
import com.almudier.mygarage.core.presentation.components.GarageListIcon
import com.almudier.mygarage.core.presentation.components.GarageListItem
import com.almudier.mygarage.core.presentation.components.GarageSectionHeader
import com.almudier.mygarage.core.presentation.shared.CarVisual
import com.almudier.mygarage.core.presentation.shared.LocalCurrency
import com.almudier.mygarage.core.presentation.shared.color
import com.almudier.mygarage.core.presentation.shared.formatMoney
import com.almudier.mygarage.core.presentation.shared.reminderColor
import com.almudier.mygarage.core.presentation.shared.reminderDueText
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme
import java.time.LocalDate
import com.almudier.mygarage.core.presentation.R as CoreR
import com.almudier.mygarage.garage.R as ResR

@Composable
fun GarageScreen(viewModel: GarageViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    GarageScreenContent(
        vehicles = state.vehicles,
        records = state.records,
        intervals = state.intervals,
        selectedVehicleId = state.selectedVehicleId,
        isLoading = state.isLoading,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun GarageScreenContent(
    vehicles: List<Vehicle>,
    records: Map<String, List<MaintenanceRecord>>,
    intervals: List<ServiceInterval>,
    selectedVehicleId: String?,
    isLoading: Boolean,
    onEvent: (GarageContract.Intent) -> Unit,
) {
    val spacing = GarageDesignTheme.spacing
    Box(Modifier.fillMaxSize().background(GarageDesignTheme.colors.background)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.nr)
                .padding(bottom = 110.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(top = spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(ResR.string.garage_title), style = GarageDesignTheme.typography.h1, modifier = Modifier.weight(1f))
                GarageIconButton(Icons.Rounded.Settings, stringResource(CoreR.string.common_settings), onClick = { onEvent(GarageContract.Intent.OpenSettings) })
            }
            when {
                isLoading && vehicles.isEmpty() -> Box(Modifier.fillMaxWidth().padding(vertical = spacing.xxl), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GarageDesignTheme.colors.primary)
                }
                vehicles.isEmpty() -> WelcomeContent()
                else -> {
                    Text(
                        stringResource(ResR.string.garage_subtitle),
                        style = GarageDesignTheme.typography.body.copy(color = GarageDesignTheme.colors.dim),
                        modifier = Modifier.padding(bottom = spacing.nr),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        val today = remember { LocalDate.now() }
                        vehicles.forEach { vehicle ->
                            val vehicleRecords = records[vehicle.id].orEmpty()
                            val urgent = remember(vehicleRecords, intervals) {
                                reminderStatuses(intervals, vehicleRecords, today)
                                    .firstOrNull { it.state == ReminderState.DueSoon || it.state == ReminderState.Overdue }
                            }
                            VehicleCard(
                                vehicle = vehicle.withServiceMileage(vehicleRecords),
                                recordCount = vehicleRecords.size,
                                totalSpent = vehicleRecords.sumOf { it.cost },
                                urgentReminder = urgent,
                                selected = vehicle.id == selectedVehicleId,
                                onClick = { onEvent(GarageContract.Intent.OpenVehicle(vehicle.id)) },
                            )
                        }
                    }
                }
            }
        }
        GarageButton(
            text = stringResource(if (vehicles.isEmpty()) ResR.string.garage_add_first else ResR.string.garage_add_button),
            onClick = { onEvent(GarageContract.Intent.AddVehicle) },
            leadingIcon = Icons.Rounded.Add,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = spacing.nr, vertical = spacing.sm),
        )
    }
}

/** A car on the Garage screen: picture, make · year, model, records and all-time spend, due badge. */
@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    recordCount: Int,
    totalSpent: Double,
    urgentReminder: ReminderStatus?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val typography = GarageDesignTheme.typography
    GarageCard(Modifier.fillMaxWidth(), selected = selected, onClick = onClick) {
        CarVisual(vehicle, Modifier.fillMaxWidth().height(110.dp))
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${vehicle.manufacturer} · ${vehicle.year}".uppercase(), style = typography.eyebrow.copy(fontSize = 10.sp))
                Text(listOf(vehicle.model, vehicle.trim).filter { it.isNotBlank() }.joinToString(" "), style = typography.h2.copy(fontSize = 17.sp))
                Text(
                    "${pluralStringResource(CoreR.plurals.records_count, recordCount, recordCount)} · ${formatMoney(totalSpent, LocalCurrency.current)}",
                    style = typography.mono,
                )
            }
            GarageChevron()
        }
        if (urgentReminder != null) {
            Spacer(Modifier.height(10.dp))
            GarageBadge(
                text = "${urgentReminder.interval.name} ${reminderDueText(urgentReminder)}",
                color = reminderColor(urgentReminder.state),
                icon = Icons.Rounded.Schedule,
            )
        }
    }
}

/** No cars yet: what the app tracks, with one action (the button pinned at the bottom). */
@Composable
private fun WelcomeContent() {
    val colors = GarageDesignTheme.colors
    Column(Modifier.padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(ResR.string.welcome_eyebrow).uppercase(), style = GarageDesignTheme.typography.eyebrow.copy(color = colors.primary))
        Text(stringResource(ResR.string.welcome_title), style = GarageDesignTheme.typography.display)
        Text(stringResource(ResR.string.welcome_subtitle), style = GarageDesignTheme.typography.body.copy(color = colors.dim))
        Spacer(Modifier.height(6.dp))
        GarageSectionHeader(stringResource(ResR.string.welcome_tracks))
        GarageListGroup {
            GarageListItem(
                leading = { GarageListIcon(Icons.Rounded.CarRepair, tint = RecordType.Maintenance.color) },
                headline = { Text(stringResource(CoreR.string.common_maintenance)) },
                supporting = { Text(stringResource(ResR.string.welcome_track_maintenance)) },
            )
            GarageListItem(
                leading = { GarageListIcon(Icons.Rounded.Build, tint = RecordType.Upgrade.color) },
                headline = { Text(stringResource(CoreR.string.type_plural_upgrade)) },
                supporting = { Text(stringResource(ResR.string.welcome_track_upgrades)) },
            )
            GarageListItem(
                leading = { GarageListIcon(Icons.Rounded.LocalCarWash, tint = RecordType.Detailing.color) },
                headline = { Text(stringResource(CoreR.string.common_detailing)) },
                supporting = { Text(stringResource(ResR.string.welcome_track_detailing)) },
            )
        }
    }
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun GarageScreenPreview() {
    val bmw = Vehicle("bmw", "BMW", "330e", 2020, "M Sport", 48600, "21-88330", color = "Mineral grey")
    GarageScreenContent(
        vehicles = listOf(bmw, Vehicle("ford", "Ford", "Fusion", 2017, "Titanium", 142350, "", color = "Blue")),
        records = mapOf(
            "bmw" to listOf(
                MaintenanceRecord("1", "bmw", "2026-05-06", 48000, "Bavaria Motors", 35.0, items = listOf(MaintenanceItem("Oil change"))),
                MaintenanceRecord("2", "bmw", "2026-09-22", 0, "Tune Lab", 1480.0, items = listOf(MaintenanceItem("Tune")), type = RecordType.Upgrade),
            ),
        ),
        intervals = DefaultServiceIntervals,
        selectedVehicleId = "bmw",
        isLoading = false,
        onEvent = {},
    )
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun GarageWelcomePreview() {
    GarageScreenContent(emptyList(), emptyMap(), DefaultServiceIntervals, null, isLoading = false, onEvent = {})
}
