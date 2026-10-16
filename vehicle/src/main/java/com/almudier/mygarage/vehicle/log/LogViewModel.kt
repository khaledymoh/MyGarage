package com.almudier.mygarage.vehicle.log

import androidx.lifecycle.viewModelScope
import com.almudier.mygarage.core.presentation.components.SnackbarManager
import com.almudier.mygarage.core.presentation.mvi.BaseMviViewModel
import com.almudier.mygarage.core.presentation.navigation.Navigator
import com.almudier.mygarage.core.presentation.toErrorMessageRes
import com.almudier.mygarage.maintenance.api.MaintenanceDetailRoute
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
class LogViewModel @Inject constructor(
    private val navigator: Navigator,
    private val loader: CarRecordsLoader,
    private val snackbarManager: SnackbarManager,
) : BaseMviViewModel<LogContract.Intent, LogContract.State, LogContract.Effect>(LogContract.State()) {

    private var vehicleId: String? = null
    private var loadJob: Job? = null

    override fun onEvent(intent: LogContract.Intent) {
        when (intent) {
            is LogContract.Intent.Load -> load(intent.vehicleId)
            is LogContract.Intent.Query -> setState { copy(filters = filters.copy(query = intent.value)) }
            is LogContract.Intent.QuickType -> setState {
                copy(filters = filters.copy(types = intent.type?.let { setOf(it) } ?: emptySet()))
            }
            LogContract.Intent.OpenFilters -> setState { copy(draft = filters) }
            LogContract.Intent.CloseFilters -> setState { copy(draft = null) }
            is LogContract.Intent.EditDraft -> setState { copy(draft = intent.draft) }
            LogContract.Intent.ApplyDraft -> setState { copy(filters = draft ?: filters, draft = null) }
            LogContract.Intent.ClearFilters -> setState { copy(filters = LogFilters(), draft = null) }
            is LogContract.Intent.OpenRecord -> vehicleId?.let { navigator.navigate(MaintenanceDetailRoute(it, intent.id)) }
            LogContract.Intent.OpenProfile -> vehicleId?.let { navigator.navigate(VehicleProfileRoute(it)) }
            LogContract.Intent.SwitchCar -> loader.switchCar()
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
