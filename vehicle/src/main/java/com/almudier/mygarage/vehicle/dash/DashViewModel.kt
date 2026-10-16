package com.almudier.mygarage.vehicle.dash

import androidx.lifecycle.viewModelScope
import com.almudier.mygarage.core.domain.model.RecordType
import com.almudier.mygarage.core.domain.repository.AppSettingsRepository
import com.almudier.mygarage.core.presentation.components.SnackbarManager
import com.almudier.mygarage.core.presentation.mvi.BaseMviViewModel
import com.almudier.mygarage.core.presentation.navigation.Navigator
import com.almudier.mygarage.core.presentation.toErrorMessageRes
import com.almudier.mygarage.maintenance.api.MaintenanceDetailRoute
import com.almudier.mygarage.maintenance.api.MaintenanceFormRoute
import com.almudier.mygarage.vehicle.api.VehicleProfileRoute
import com.almudier.mygarage.vehicle.common.CarRecordsLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashViewModel @Inject constructor(
    private val navigator: Navigator,
    private val loader: CarRecordsLoader,
    private val snackbarManager: SnackbarManager,
    appSettings: AppSettingsRepository,
) : BaseMviViewModel<DashContract.Intent, DashContract.State, DashContract.Effect>(DashContract.State()) {

    private var vehicleId: String? = null
    private var loadJob: Job? = null

    init {
        appSettings.serviceIntervals.onEach { setState { copy(intervals = it) } }.launchIn(viewModelScope)
    }

    override fun onEvent(intent: DashContract.Intent) {
        when (intent) {
            is DashContract.Intent.Load -> load(intent.vehicleId)
            DashContract.Intent.OpenProfile -> vehicleId?.let { navigator.navigate(VehicleProfileRoute(it)) }
            DashContract.Intent.SwitchCar -> loader.switchCar()
            DashContract.Intent.AddRecord -> vehicleId?.let { navigator.navigate(MaintenanceFormRoute(it, type = RecordType.Maintenance.name)) }
            is DashContract.Intent.OpenRecord -> vehicleId?.let { navigator.navigate(MaintenanceDetailRoute(it, intent.id)) }
        }
    }

    private fun load(id: String) {
        if (vehicleId == id) return
        vehicleId = id
        loadJob?.cancel()
        loadJob = loader.observe(id)
            .onEach { data ->
                if (data == null) {
                    viewModelScope.launch { loader.onCarMissing() }
                    return@onEach
                }
                setState { copy(vehicle = data.vehicle, records = data.records, isLoading = false) }
            }
            .catch { snackbarManager.showError(it.toErrorMessageRes()) }
            .launchIn(viewModelScope)
    }
}
