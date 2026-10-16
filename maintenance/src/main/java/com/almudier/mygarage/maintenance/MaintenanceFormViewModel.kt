package com.almudier.mygarage.maintenance

import androidx.lifecycle.viewModelScope
import com.almudier.mygarage.core.domain.model.MaintenanceItem
import com.almudier.mygarage.core.domain.model.MaintenanceRecord
import com.almudier.mygarage.core.domain.model.RecordType
import com.almudier.mygarage.core.domain.repository.MaintenanceServiceRepository
import com.almudier.mygarage.core.domain.repository.MaintenanceRepository
import com.almudier.mygarage.core.presentation.components.SnackbarManager
import com.almudier.mygarage.core.presentation.mvi.BaseMviViewModel
import com.almudier.mygarage.core.presentation.navigation.Navigator
import com.almudier.mygarage.core.presentation.toErrorMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MaintenanceFormViewModel @Inject constructor(
    private val navigator: Navigator,
    private val maintenanceRepository: MaintenanceRepository,
    private val snackbarManager: SnackbarManager,
    private val serviceRepository: MaintenanceServiceRepository,
) : BaseMviViewModel<MaintenanceFormContract.Intent, MaintenanceFormContract.State, MaintenanceFormContract.Effect>(MaintenanceFormContract.State()) {

    private var loadedKey: Pair<String, String?>? = null

    init {
        serviceRepository.observeServices()
            .onEach { services -> setState { copy(services = services) } }
            .catch { }
            .launchIn(viewModelScope)
    }

    override fun onEvent(intent: MaintenanceFormContract.Intent) {
        when (intent) {
            is MaintenanceFormContract.Intent.Load -> load(intent.vehicleId, intent.recordId, intent.type)
            is MaintenanceFormContract.Intent.Update -> setState {
                when (intent.field) {
                    "date" -> copy(date = intent.value)
                    "mileage" -> copy(mileage = intent.value.filter(Char::isDigit))
                    "workshop" -> copy(workshop = intent.value)
                    "notes" -> copy(notes = intent.value)
                    else -> this
                }
            }
            is MaintenanceFormContract.Intent.UpdateItem -> setState {
                copy(
                    items = items.mapIndexed { i, item ->
                        if (i != intent.index) item else when (intent.field) {
                            "service" -> item.copy(service = intent.value)
                            "cost" -> item.copy(cost = intent.value.decimalOnly())
                            "labor" -> item.copy(laborCost = intent.value.decimalOnly())
                            "notes" -> item.copy(notes = intent.value)
                            else -> item
                        }
                    },
                )
            }
            is MaintenanceFormContract.Intent.RemoveItem -> setState {
                copy(items = items.filterIndexed { i, _ -> i != intent.index })
            }
            is MaintenanceFormContract.Intent.SetType -> setState { copy(type = intent.type) }
            is MaintenanceFormContract.Intent.SetReceipt -> setState { copy(receipt = intent.uri) }
            is MaintenanceFormContract.Intent.Preset -> setState {
                val first = items.firstOrNull() ?: MaintenanceFormContract.ItemForm()
                copy(items = listOf(first.copy(service = intent.service)) + items.drop(1))
            }
            MaintenanceFormContract.Intent.AddItem -> setState {
                copy(items = items + MaintenanceFormContract.ItemForm())
            }
            MaintenanceFormContract.Intent.Back -> navigator.back()
            MaintenanceFormContract.Intent.Save -> save()
        }
    }

    private fun load(vehicleId: String, recordId: String?, type: RecordType?) {
        val key = vehicleId to recordId
        if (loadedKey == key) return
        loadedKey = key
        if (recordId == null) {
            setState { copy(vehicleId = vehicleId, recordId = null, date = LocalDate.now().toString(), type = type ?: RecordType.Maintenance) }
            return
        }
        setState { copy(vehicleId = vehicleId, recordId = recordId, isLoading = true) }
        viewModelScope.launch {
            val record = try {
                maintenanceRepository.observeRecord(vehicleId, recordId).first()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                snackbarManager.showError(e.toErrorMessageRes())
                null
            }
            if (record?.hasReceipt == true) {
                launch {
                    maintenanceRepository.loadReceipt(vehicleId, recordId)
                        .onSuccess { receipt -> setState { copy(receipt = receipt) } }
                }
            }
            setState {
                if (record == null) {
                    copy(isLoading = false)
                } else {
                    copy(
                        isLoading = false,
                        date = record.date,
                        mileage = if (record.mileage > 0) record.mileage.toString() else "",
                        workshop = record.workshop,
                        notes = record.notes,
                        type = record.type,
                        items = record.items.map {
                            MaintenanceFormContract.ItemForm(
                                service = it.service,
                                cost = it.cost.asInput(),
                                laborCost = it.laborCost.asInput(),
                                notes = it.notes,
                            )
                        }.ifEmpty { listOf(MaintenanceFormContract.ItemForm()) },
                    )
                }
            }
        }
    }

    private fun save() {
        val state = uiState.value
        if (state.isSaving || !state.canSave) return
        val record = MaintenanceRecord(
            id = state.recordId.orEmpty(),
            vehicleId = state.vehicleId,
            date = state.date,
            mileage = if (state.type.tracksMileage) state.mileage.toIntOrNull() ?: 0 else 0,
            hasReceipt = state.receipt != null,
            receipt = state.receipt,
            type = state.type,
            workshop = state.workshop.trim(),
            totalCost = state.totalCost,
            notes = state.notes.trim(),
            items = state.items.map {
                MaintenanceItem(
                    service = it.service.trim(),
                    cost = it.cost.toDouble(),
                    laborCost = it.laborCost.toDoubleOrNull() ?: 0.0,
                    notes = it.notes.trim(),
                )
            },
        )
        viewModelScope.launch {
            setState { copy(isSaving = true) }
            maintenanceRepository.saveRecord(record)
                .onSuccess {
                    val known = state.services.map { it.lowercase() }.toSet()
                    serviceRepository.addServices(record.items.map { it.service }.filter { it.lowercase() !in known })
                    navigator.back()
                }
                .onFailure { snackbarManager.showError(it.toErrorMessageRes()) }
            setState { copy(isSaving = false) }
        }
    }

    private fun Double.asInput(): String =
        if (this % 1.0 == 0.0) toLong().toString() else toString()

    private fun String.decimalOnly(): String {
        var seenDot = false
        return filter { c ->
            when {
                c.isDigit() -> true
                c == '.' && !seenDot -> { seenDot = true; true }
                else -> false
            }
        }
    }
}
