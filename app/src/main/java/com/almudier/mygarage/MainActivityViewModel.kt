package com.almudier.mygarage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.almudier.mygarage.authentication.api.AuthRoute
import com.almudier.mygarage.core.data.datastore.UserPreferencesDataSource
import com.almudier.mygarage.core.domain.repository.AppSettingsRepository
import com.almudier.mygarage.garage.api.GarageRoute
import com.almudier.mygarage.update.AppUpdate
import com.almudier.mygarage.update.AppUpdateChecker
import com.almudier.mygarage.vehicle.api.CarHomeRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface StartDestinationUiState {
    data object Loading : StartDestinationUiState
    data class Ready(val startRoute: NavKey) : StartDestinationUiState
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userPreferencesDataSource: UserPreferencesDataSource,
    appSettings: AppSettingsRepository,
    updateChecker: AppUpdateChecker,
) : ViewModel() {

    private val laterVersion = MutableStateFlow<String?>(null)

    /** A newer release to offer; an optional one stays hidden after "Later" for this session. */
    val update: StateFlow<AppUpdate?> = combine(updateChecker.update, laterVersion) { update, later ->
        update?.takeIf { it.required || it.versionName != later }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun postponeUpdate() {
        laterVersion.value = update.value?.versionName
    }

    /** The user's currency, provided to every screen as LocalCurrency. */
    val currency: StateFlow<String> = appSettings.currency
        .stateIn(viewModelScope, SharingStarted.Eagerly, "JOD")


    val uiState: StateFlow<StartDestinationUiState> = userPreferencesDataSource.userPreferences
        .map<_, StartDestinationUiState> { preferences ->
            val signedIn = preferences.isSignedIn && preferences.uid != null
            val selected = preferences.selectedVehicleId
            val start = when {
                !signedIn -> AuthRoute
                selected != null -> CarHomeRoute(selected)
                else -> GarageRoute
            }
            StartDestinationUiState.Ready(start)
        }
        // The start route is decided once; later preference changes navigate explicitly.
        .take(1)
        .stateIn(viewModelScope, SharingStarted.Eagerly, StartDestinationUiState.Loading)
}
