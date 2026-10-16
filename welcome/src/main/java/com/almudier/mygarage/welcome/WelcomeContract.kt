package com.almudier.mygarage.welcome

import com.almudier.mygarage.core.presentation.mvi.UiEffect
import com.almudier.mygarage.core.presentation.mvi.UiIntent
import com.almudier.mygarage.core.presentation.mvi.UiState

object WelcomeContract {

    data object State : UiState

    sealed interface Intent : UiIntent {
        data object AddFirstVehicle : Intent
    }

    sealed interface Effect : UiEffect
}
