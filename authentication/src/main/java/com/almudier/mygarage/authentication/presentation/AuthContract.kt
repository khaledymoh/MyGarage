package com.almudier.mygarage.authentication.presentation

import android.content.Context
import com.almudier.mygarage.core.presentation.mvi.UiEffect
import com.almudier.mygarage.core.presentation.mvi.UiIntent
import com.almudier.mygarage.core.presentation.mvi.UiState

object AuthContract {

    data class State(
        val isLoading: Boolean = false,
    ) : UiState

    sealed interface Intent : UiIntent {
        data class ContinueWithGoogleClicked(val activityContext: Context) : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateToMain : Effect
    }
}
