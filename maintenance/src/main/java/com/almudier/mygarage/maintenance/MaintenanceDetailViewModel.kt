package com.almudier.mygarage.maintenance

import androidx.lifecycle.viewModelScope
import com.almudier.mygarage.core.domain.model.EmptyMaintenanceRecord
import com.almudier.mygarage.core.domain.repository.MaintenanceRepository
import com.almudier.mygarage.core.presentation.components.SnackbarManager
import com.almudier.mygarage.core.presentation.mvi.BaseMviViewModel
import com.almudier.mygarage.core.presentation.navigation.Navigator
import com.almudier.mygarage.core.presentation.toErrorMessageRes
import com.almudier.mygarage.maintenance.api.MaintenanceFormRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaintenanceDetailViewModel @Inject constructor(
    private val navigator: Navigator,
    private val maintenanceRepository: MaintenanceRepository,
    private val snackbarManager: SnackbarManager,
) : BaseMviViewModel<MaintenanceDetailContract.Intent, MaintenanceDetailContract.State, MaintenanceDetailContract.Effect>(MaintenanceDetailContract.State()) {

    private var loadJob: Job? = null
    private var loadedKey: Pair<String, String>? = null

    override fun onEvent(intent: MaintenanceDetailContract.Intent) {
        when (intent) {
            is MaintenanceDetailContract.Intent.Load -> load(intent.vehicleId, intent.id)
            MaintenanceDetailContract.Intent.Back -> navigator.back()
            MaintenanceDetailContract.Intent.Edit -> navigator.navigate(MaintenanceFormRoute(uiState.value.record.vehicleId, uiState.value.record.id))
            MaintenanceDetailContract.Intent.AskDelete -> setState { copy(showDelete = true) }
            MaintenanceDetailContract.Intent.CancelDelete -> setState { copy(showDelete = false) }
            MaintenanceDetailContract.Intent.ConfirmDelete -> delete()
            is MaintenanceDetailContract.Intent.ShowReceipt -> setState { copy(showReceipt = intent.show) }
        }
    }

    private fun load(vehicleId: String, id: String) {
        val key = vehicleId to id
        if (loadedKey == key) return
        loadedKey = key
        setState { copy(record = EmptyMaintenanceRecord.copy(id = id, vehicleId = vehicleId)) }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            maintenanceRepository.observeRecord(vehicleId, id)
                .catch { snackbarManager.showError(it.toErrorMessageRes()) }
                .collect { record ->
                    if (record == null) return@collect
                    setState { copy(record = record, receipt = if (record.hasReceipt) receipt else null) }
                    if (record.hasReceipt && uiState.value.receipt == null) loadReceipt(vehicleId, id)
                }
        }
    }

    private var receiptJob: Job? = null

    private fun loadReceipt(vehicleId: String, id: String) {
        if (receiptJob?.isActive == true) return
        receiptJob = viewModelScope.launch {
            maintenanceRepository.loadReceipt(vehicleId, id)
                .onSuccess { receipt -> setState { copy(receipt = receipt) } }
        }
    }

    private fun delete() {
        val state = uiState.value
        if (state.isDeleting) return
        setState { copy(showDelete = false, isDeleting = true) }
        viewModelScope.launch {
            maintenanceRepository.deleteRecord(state.record.vehicleId, state.record.id)
                .onSuccess { navigator.back() }
                .onFailure {
                    setState { copy(isDeleting = false) }
                    snackbarManager.showError(it.toErrorMessageRes())
                }
        }
    }
}
