package com.almudier.mygarage.settings

import com.almudier.mygarage.core.domain.model.ServiceInterval
import com.almudier.mygarage.core.presentation.mvi.UiEffect
import com.almudier.mygarage.core.presentation.mvi.UiIntent
import com.almudier.mygarage.core.presentation.mvi.UiState

object SettingsContract {

    data class State(
        val dialog: Dialog? = null,
        val sheet: Sheet? = null,
        val currency: String = "JOD",
        val intervals: List<ServiceInterval> = emptyList(),
        /** Sign-out is running: the dialog stays open with a spinner on its button. */
        val signingOut: Boolean = false,
    ) : UiState

    enum class Dialog { CLEAR, SIGN_OUT }

    enum class Sheet { CURRENCY, INTERVALS }

    sealed interface Intent : UiIntent {
        data class Show(val dialog: Dialog) : Intent
        data object Dismiss : Intent
        data object Confirm : Intent
        data class OpenSheet(val sheet: Sheet?) : Intent
        data class SetCurrency(val code: String) : Intent
        data class SetIntervalMonths(val id: String, val months: Int) : Intent
    }

    sealed interface Effect : UiEffect
}
