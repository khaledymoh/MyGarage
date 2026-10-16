package com.almudier.mygarage.settings

import androidx.lifecycle.viewModelScope
import com.almudier.mygarage.authentication.api.AuthRoute
import com.almudier.mygarage.authentication.api.SignOutUseCase
import com.almudier.mygarage.core.domain.repository.AppSettingsRepository
import com.almudier.mygarage.core.presentation.mvi.BaseMviViewModel
import com.almudier.mygarage.core.presentation.navigation.Navigator
import com.almudier.mygarage.welcome.api.WelcomeRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val navigator: Navigator,
    private val signOutUseCase: SignOutUseCase,
    private val appSettings: AppSettingsRepository,
) : BaseMviViewModel<SettingsContract.Intent, SettingsContract.State, SettingsContract.Effect>(SettingsContract.State()) {

    init {
        appSettings.currency.onEach { setState { copy(currency = it) } }.launchIn(viewModelScope)
        appSettings.serviceIntervals.onEach { setState { copy(intervals = it) } }.launchIn(viewModelScope)
    }

    override fun onEvent(intent: SettingsContract.Intent) {
        when (intent) {
            is SettingsContract.Intent.Show -> setState { copy(dialog = intent.dialog) }
            SettingsContract.Intent.Dismiss -> if (!uiState.value.signingOut) setState { copy(dialog = null) }
            SettingsContract.Intent.Confirm -> when (uiState.value.dialog) {
                SettingsContract.Dialog.CLEAR -> navigator.replaceAll(WelcomeRoute)
                SettingsContract.Dialog.SIGN_OUT -> signOut()
                null -> setState { copy(dialog = null) }
            }
            is SettingsContract.Intent.OpenSheet -> setState { copy(sheet = intent.sheet) }
            is SettingsContract.Intent.SetCurrency -> viewModelScope.launch {
                appSettings.setCurrency(intent.code)
                setState { copy(sheet = null) }
            }
            is SettingsContract.Intent.SetIntervalMonths -> viewModelScope.launch {
                appSettings.setIntervalMonths(intent.id, intent.months)
            }
        }
    }

    private fun signOut() {
        if (uiState.value.signingOut) return
        setState { copy(signingOut = true) }
        viewModelScope.launch {
            try {
                signOutUseCase()
                navigator.replaceAll(AuthRoute)
            } finally {
                setState { copy(signingOut = false, dialog = null) }
            }
        }
    }
}
