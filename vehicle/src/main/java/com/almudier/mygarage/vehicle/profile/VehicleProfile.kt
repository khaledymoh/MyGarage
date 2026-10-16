package com.almudier.mygarage.vehicle.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.almudier.mygarage.core.presentation.components.GarageGlassScaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.almudier.mygarage.core.domain.model.EmptyVehicle
import com.almudier.mygarage.core.domain.model.MaintenanceRecord
import com.almudier.mygarage.core.domain.model.RecordType
import com.almudier.mygarage.core.domain.model.Vehicle
import com.almudier.mygarage.core.domain.model.cost
import com.almudier.mygarage.core.domain.model.totalsByType
import com.almudier.mygarage.core.domain.repository.AppSettingsRepository
import com.almudier.mygarage.core.domain.repository.VehicleRepository
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.components.GarageCard
import com.almudier.mygarage.core.presentation.components.GarageDialog
import com.almudier.mygarage.core.presentation.components.GarageDialogButton
import com.almudier.mygarage.core.presentation.components.GarageIconButton
import com.almudier.mygarage.core.presentation.components.GarageIconButtonStyle
import com.almudier.mygarage.core.presentation.components.GarageListGroup
import com.almudier.mygarage.core.presentation.components.GarageListIcon
import com.almudier.mygarage.core.presentation.components.GarageListItem
import com.almudier.mygarage.core.presentation.components.GarageSectionHeader
import com.almudier.mygarage.core.presentation.components.GarageTone
import com.almudier.mygarage.core.presentation.components.GarageTopBar
import com.almudier.mygarage.core.presentation.components.SnackbarManager
import com.almudier.mygarage.core.presentation.formatNumber
import com.almudier.mygarage.core.presentation.mvi.BaseMviViewModel
import com.almudier.mygarage.core.presentation.mvi.UiEffect
import com.almudier.mygarage.core.presentation.mvi.UiIntent
import com.almudier.mygarage.core.presentation.mvi.UiState
import com.almudier.mygarage.core.presentation.navigation.Navigator
import com.almudier.mygarage.core.presentation.shared.CarVisual
import com.almudier.mygarage.core.presentation.shared.LocalCurrency
import com.almudier.mygarage.core.presentation.shared.color
import com.almudier.mygarage.core.presentation.shared.formatMoney
import com.almudier.mygarage.core.presentation.shared.icon
import com.almudier.mygarage.core.presentation.shared.pluralLabel
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme
import com.almudier.mygarage.core.presentation.toErrorMessageRes
import com.almudier.mygarage.garage.api.GarageRoute
import com.almudier.mygarage.vehicle.R
import com.almudier.mygarage.vehicle.api.VehicleFormRoute
import com.almudier.mygarage.vehicle.api.VehicleProfileRoute
import com.almudier.mygarage.vehicle.common.CarRecordsLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

object VehicleProfileContract {
    data class State(
        val vehicle: Vehicle = EmptyVehicle,
        val records: List<MaintenanceRecord> = emptyList(),
        val showDelete: Boolean = false,
        val isDeleting: Boolean = false,
    ) : UiState

    sealed interface Intent : UiIntent {
        data class Load(val vehicleId: String) : Intent
        data object Back : Intent
        data object Edit : Intent
        data object AskDelete : Intent
        data object CancelDelete : Intent
        data object ConfirmDelete : Intent
    }

    sealed interface Effect : UiEffect
}

@HiltViewModel
class VehicleProfileViewModel @Inject constructor(
    private val navigator: Navigator,
    private val loader: CarRecordsLoader,
    private val vehicleRepository: VehicleRepository,
    private val appSettings: AppSettingsRepository,
    private val snackbarManager: SnackbarManager,
) : BaseMviViewModel<VehicleProfileContract.Intent, VehicleProfileContract.State, VehicleProfileContract.Effect>(VehicleProfileContract.State()) {

    private var vehicleId: String? = null
    private var loadJob: Job? = null

    override fun onEvent(intent: VehicleProfileContract.Intent) {
        when (intent) {
            is VehicleProfileContract.Intent.Load -> load(intent.vehicleId)
            VehicleProfileContract.Intent.Back -> navigator.back()
            VehicleProfileContract.Intent.Edit -> vehicleId?.let { navigator.navigate(VehicleFormRoute(it)) }
            VehicleProfileContract.Intent.AskDelete -> setState { copy(showDelete = true) }
            VehicleProfileContract.Intent.CancelDelete -> setState { copy(showDelete = false) }
            VehicleProfileContract.Intent.ConfirmDelete -> delete()
        }
    }

    private fun load(id: String) {
        if (vehicleId == id) return
        vehicleId = id
        loadJob?.cancel()
        loadJob = loader.observe(id)
            .onEach { data -> if (data != null) setState { copy(vehicle = data.vehicle, records = data.records) } }
            .catch { snackbarManager.showError(it.toErrorMessageRes()) }
            .launchIn(viewModelScope)
    }

    private fun delete() {
        val id = vehicleId ?: return
        if (uiState.value.isDeleting) return
        setState { copy(showDelete = false, isDeleting = true) }
        viewModelScope.launch {
            loadJob?.cancel()
            vehicleRepository.deleteVehicle(id)
                .onSuccess {
                    appSettings.selectVehicle(null)
                    navigator.replaceAll(GarageRoute)
                }
                .onFailure {
                    setState { copy(isDeleting = false) }
                    snackbarManager.showError(it.toErrorMessageRes())
                }
        }
    }
}

@Composable
fun VehicleProfileScreen(route: VehicleProfileRoute, viewModel: VehicleProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(route.vehicleId) { viewModel.onEvent(VehicleProfileContract.Intent.Load(route.vehicleId)) }
    VehicleProfileContent(state.vehicle, state.records, state.showDelete, state.isDeleting, viewModel::onEvent)
}

@Composable
fun VehicleProfileContent(
    vehicle: Vehicle,
    records: List<MaintenanceRecord>,
    showDelete: Boolean,
    isDeleting: Boolean,
    onEvent: (VehicleProfileContract.Intent) -> Unit,
) {
    val colors = GarageDesignTheme.colors
    val spacing = GarageDesignTheme.spacing
    val currency = LocalCurrency.current
    val totals = totalsByType(records)
    val name = listOf(vehicle.year.takeIf { it > 0 }?.toString(), vehicle.model, vehicle.trim).filterNot { it.isNullOrBlank() }.joinToString(" ")

    GarageGlassScaffold(
        topBar = {
        GarageTopBar(
            containerColor = Color.Transparent,
            navigationIcon = { GarageIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.profile_back), onClick = { onEvent(VehicleProfileContract.Intent.Back) }) },
            actions = {
                GarageIconButton(Icons.Rounded.Edit, stringResource(R.string.profile_edit), onClick = { onEvent(VehicleProfileContract.Intent.Edit) })
                GarageIconButton(Icons.Rounded.Delete, stringResource(R.string.profile_delete), onClick = { onEvent(VehicleProfileContract.Intent.AskDelete) }, style = GarageIconButtonStyle.Danger, enabled = !isDeleting)
            },
        )
        },
    ) { glassPadding ->
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(top = glassPadding.calculateTopPadding() + spacing.xs)
                .padding(horizontal = spacing.nr).navigationBarsPadding().padding(bottom = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            CarVisual(vehicle, Modifier.fillMaxWidth().height(150.dp))
            Column {
                Text(vehicle.manufacturer.uppercase(), style = GarageDesignTheme.typography.eyebrow.copy(color = colors.primary, fontSize = 10.sp))
                Text(name, style = GarageDesignTheme.typography.h1)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Spec(stringResource(R.string.profile_last_odometer), "${formatNumber(vehicle.mileage)} km", Modifier.weight(1f))
                Spec(stringResource(R.string.profile_plate), vehicle.plate.ifBlank { stringResource(R.string.profile_none) }, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Spec(stringResource(R.string.profile_color), vehicle.color.ifBlank { stringResource(R.string.profile_none) }, Modifier.weight(1f))
                Spec(stringResource(R.string.profile_year), if (vehicle.year > 0) vehicle.year.toString() else stringResource(R.string.profile_none), Modifier.weight(1f))
            }
            if (vehicle.vin.isNotBlank()) Spec(stringResource(R.string.profile_vin), vehicle.vin, Modifier.fillMaxWidth())

            GarageSectionHeader(stringResource(R.string.profile_at_a_glance), Modifier.padding(top = spacing.xs))
            GarageListGroup(Modifier.fillMaxWidth()) {
                RecordType.entries.forEach { type ->
                    val typeRecords = records.filter { it.type == type }
                    GarageListItem(
                        leading = { GarageListIcon(type.icon, tint = type.color) },
                        headline = { Text(stringResource(type.pluralLabel)) },
                        supporting = {
                            Text(
                                when (type) {
                                    RecordType.Maintenance -> pluralStringResource(R.plurals.profile_visits, typeRecords.size, typeRecords.size)
                                    RecordType.Upgrade -> pluralStringResource(R.plurals.profile_upgrades, typeRecords.size, typeRecords.size, typeRecords.sumOf { it.items.size })
                                    RecordType.Detailing -> pluralStringResource(R.plurals.profile_washes, typeRecords.size, typeRecords.size)
                                }
                            )
                        },
                        trailing = { Text(formatMoney(totals.getValue(type), currency), color = colors.text) },
                    )
                }
            }
        }
    }

    if (showDelete) {
        GarageDialog(
            onDismissRequest = { onEvent(VehicleProfileContract.Intent.CancelDelete) },
            icon = Icons.Rounded.Delete,
            title = { Text(stringResource(R.string.profile_delete_title, "${vehicle.year} ${vehicle.manufacturer} ${vehicle.model}".trim())) },
            text = { Text(pluralStringResource(R.plurals.profile_delete_text, records.size, records.size, formatMoney(records.sumOf { it.cost }, currency))) },
            dismissButton = { GarageDialogButton(stringResource(com.almudier.mygarage.core.presentation.R.string.common_cancel), onClick = { onEvent(VehicleProfileContract.Intent.CancelDelete) }) },
            confirmButton = { GarageDialogButton(stringResource(R.string.profile_delete), onClick = { onEvent(VehicleProfileContract.Intent.ConfirmDelete) }, tone = GarageTone.Danger, emphasized = true) },
        )
    }
}

@Composable
private fun Spec(label: String, value: String, modifier: Modifier = Modifier) {
    GarageCard(modifier) {
        GarageSectionHeader(label)
        Text(value, style = GarageDesignTheme.typography.mono.copy(fontSize = 13.sp, color = GarageDesignTheme.colors.text), modifier = Modifier.padding(top = 2.dp))
    }
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun VehicleProfilePreview() {
    VehicleProfileContent(
        vehicle = Vehicle("v", "BMW", "330e", 2020, "M Sport", 48600, "21-88330", vin = "WBA5P7C0XXXXX4821", color = "Mineral grey"),
        records = emptyList(),
        showDelete = false,
        isDeleting = false,
        onEvent = {},
    )
}
