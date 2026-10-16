package com.almudier.mygarage.authentication.presentation

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.almudier.mygarage.authentication.R
import com.almudier.mygarage.authentication.domain.model.AuthError
import com.almudier.mygarage.authentication.domain.model.AuthException
import com.almudier.mygarage.authentication.domain.usecase.SignInWithGoogleUseCase
import com.almudier.mygarage.core.domain.NetworkException
import com.almudier.mygarage.core.presentation.components.SnackbarManager
import com.almudier.mygarage.core.presentation.mvi.BaseMviViewModel
import com.almudier.mygarage.core.presentation.toMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val snackbarManager: SnackbarManager,
) : BaseMviViewModel<AuthContract.Intent, AuthContract.State, AuthContract.Effect>(
    AuthContract.State()
) {

    override fun onEvent(intent: AuthContract.Intent) {
        when (intent) {
            is AuthContract.Intent.ContinueWithGoogleClicked -> signIn(intent.activityContext)
        }
    }

    private fun signIn(activityContext: Context) {
        if (uiState.value.isLoading) return
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            signInWithGoogleUseCase(activityContext)
                .onSuccess {
                    setState { copy(isLoading = false) }
                    sendEffect(AuthContract.Effect.NavigateToMain)
                }
                .onFailure { throwable ->
                    setState { copy(isLoading = false) }
                    throwable.toAuthMessageRes()?.let(snackbarManager::showError)
                }
        }
    }
}

@StringRes
private fun Throwable.toAuthMessageRes(): Int? = when (this) {
    is NetworkException -> error.toMessageRes()
    is AuthException -> when (error) {
        AuthError.Cancelled -> null
        AuthError.NoGoogleAccount -> R.string.auth_error_no_google_account
        AuthError.NoPlayServices -> R.string.auth_error_no_play_services
        is AuthError.Unknown -> R.string.auth_error_generic
    }
    else -> R.string.auth_error_generic
}
