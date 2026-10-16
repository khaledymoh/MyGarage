package com.almudier.mygarage.core.presentation.components

import androidx.annotation.StringRes
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

data class SnackbarEvent(
    @param:StringRes val messageRes: Int,
    val type: GarageSnackbarType,
)

@Singleton
class SnackbarManager @Inject constructor() {
    private val _events = MutableSharedFlow<SnackbarEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<SnackbarEvent> = _events.asSharedFlow()

    fun showError(@StringRes messageRes: Int) = show(messageRes, GarageSnackbarType.Error)

    fun showSuccess(@StringRes messageRes: Int) = show(messageRes, GarageSnackbarType.Success)

    fun showInfo(@StringRes messageRes: Int) = show(messageRes, GarageSnackbarType.Info)

    private fun show(@StringRes messageRes: Int, type: GarageSnackbarType) {
        _events.tryEmit(SnackbarEvent(messageRes, type))
    }
}
