package com.almudier.mygarage.garage

import androidx.lifecycle.viewModelScope
import com.almudier.mygarage.core.domain.repository.AppSettingsRepository
import com.almudier.mygarage.core.domain.repository.MaintenanceRepository
import com.almudier.mygarage.core.domain.repository.VehicleRepository
import com.almudier.mygarage.core.presentation.components.SnackbarManager
import com.almudier.mygarage.core.presentation.mvi.BaseMviViewModel
import com.almudier.mygarage.core.presentation.navigation.Navigator
import com.almudier.mygarage.core.presentation.toErrorMessageRes
import com.almudier.mygarage.settings.api.SettingsRoute
import com.almudier.mygarage.vehicle.api.CarHomeRoute
import com.almudier.mygarage.vehicle.api.VehicleFormRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GarageViewModel @Inject constructor(
    private val navigator: Navigator,
    private val snackbarManager: SnackbarManager,
    private val appSettings: AppSettingsRepository,
    vehicleRepository: VehicleRepository,
    maintenanceRepository: MaintenanceRepository,
) : BaseMviViewModel<GarageContract.Intent, GarageContract.State, GarageContract.Effect>(GarageContract.State()) {

    init {
        vehicleRepository.observeVehicles()
            .onEach { vehicles -> setState { copy(vehicles = vehicles, isLoading = false) } }
            .flatMapLatest { vehicles ->
                if (vehicles.isEmpty()) {
                    flowOf(emptyMap())
                } else {
                    combine(vehicles.map { vehicle ->
                        maintenanceRepository.observeRecords(vehicle.id).map { vehicle.id to it }
                    }) { it.toMap() }
                }
            }
            .onEach { records -> setState { copy(records = records) } }
            .catch { error ->
                setState { copy(isLoading = false) }
                snackbarManager.showError(error.toErrorMessageRes())
            }
            .launchIn(viewModelScope)

        appSettings.serviceIntervals.onEach { setState { copy(intervals = it) } }.launchIn(viewModelScope)
        appSettings.selectedVehicleId.onEach { setState { copy(selectedVehicleId = it) } }.launchIn(viewModelScope)
    }

    override fun onEvent(intent: GarageContract.Intent) {
        when (intent) {
            is GarageContract.Intent.OpenVehicle -> viewModelScope.launch {
                appSettings.selectVehicle(intent.id)
                navigator.replaceAll(CarHomeRoute(intent.id))
            }
            GarageContract.Intent.AddVehicle -> navigator.navigate(VehicleFormRoute())
            GarageContract.Intent.OpenSettings -> navigator.navigate(SettingsRoute)
        }
    }
}
