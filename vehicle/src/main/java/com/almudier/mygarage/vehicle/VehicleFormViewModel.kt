package com.almudier.mygarage.vehicle

import androidx.lifecycle.viewModelScope
import com.almudier.mygarage.core.domain.model.Vehicle
import com.almudier.mygarage.core.domain.repository.MaintenanceRepository
import com.almudier.mygarage.core.domain.repository.VehicleRepository
import com.almudier.mygarage.core.presentation.components.SnackbarManager
import com.almudier.mygarage.core.presentation.mvi.BaseMviViewModel
import com.almudier.mygarage.core.presentation.navigation.Navigator
import com.almudier.mygarage.core.presentation.toErrorMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleFormViewModel @Inject constructor(
    private val navigator: Navigator,
    private val vehicleRepository: VehicleRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val snackbarManager: SnackbarManager,
) : BaseMviViewModel<VehicleFormContract.Intent, VehicleFormContract.State, VehicleFormContract.Effect>(VehicleFormContract.State()) {

    override fun onEvent(intent: VehicleFormContract.Intent) {
        when (intent) {
            is VehicleFormContract.Intent.Load -> intent.id?.let { load(it) }
            is VehicleFormContract.Intent.Update -> setState {
                when (intent.field) {
                    "manufacturer" -> copy(manufacturer = intent.value)
                    "model" -> copy(model = intent.value)
                    "year" -> copy(year = intent.value.digitsOnly(MAX_YEAR_DIGITS))
                    "trim" -> copy(trim = intent.value)
                    "mileage" -> copy(mileage = intent.value.digitsOnly(MAX_MILEAGE_DIGITS))
                    "plate" -> copy(plate = intent.value)
                    "vin" -> copy(vin = intent.value)
                    "color" -> copy(color = intent.value)
                    "photoUri" -> copy(photoUri = intent.value)
                    else -> this
                }
            }
            VehicleFormContract.Intent.Back -> navigator.back()
            VehicleFormContract.Intent.Save -> save()
        }
    }

    private fun String.digitsOnly(maxLength: Int) = filter(Char::isDigit).take(maxLength)

    private fun load(id: String) {
        if (uiState.value.vehicleId == id) return
        setState { copy(editing = true, vehicleId = id, isLoading = true) }
        viewModelScope.launch {
            val vehicle = try {
                vehicleRepository.observeVehicle(id).first()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                snackbarManager.showError(e.toErrorMessageRes())
                null
            }
            val latestServiceMileage = try {
                maintenanceRepository.observeRecords(id).first().maxOfOrNull { it.mileage } ?: 0
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                0
            }
            setState {
                if (vehicle == null) {
                    copy(isLoading = false)
                } else {
                    copy(
                        isLoading = false,
                        manufacturer = vehicle.manufacturer,
                        model = vehicle.model,
                        year = vehicle.year.toString(),
                        trim = vehicle.trim,
                        mileage = maxOf(vehicle.mileage, latestServiceMileage).toString(),
                        minMileage = latestServiceMileage,
                        plate = vehicle.plate,
                        vin = vehicle.vin,
                        color = vehicle.color,
                        photoUri = vehicle.photoUri,
                    )
                }
            }
        }
    }

    private fun save() {
        val state = uiState.value
        if (state.isSaving || !state.canSave) return
        val vehicle = Vehicle(
            id = state.vehicleId.orEmpty(),
            manufacturer = state.manufacturer.trim(),
            model = state.model.trim(),
            year = state.year.toInt(),
            trim = state.trim.trim(),
            mileage = state.mileage.toInt(),
            plate = state.plate.trim(),
            vin = state.vin.trim(),
            color = state.color.trim(),
            photoUri = state.photoUri,
        )
        viewModelScope.launch {
            setState { copy(isSaving = true) }
            vehicleRepository.saveVehicle(vehicle)
                .onSuccess { navigator.back() }
                .onFailure { snackbarManager.showError(it.toErrorMessageRes()) }
            setState { copy(isSaving = false) }
        }
    }
}

private const val MAX_YEAR_DIGITS = 4
private const val MAX_MILEAGE_DIGITS = 9
