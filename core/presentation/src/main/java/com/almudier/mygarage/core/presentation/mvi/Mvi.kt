package com.almudier.mygarage.core.presentation.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface UiState
interface UiIntent
interface UiEffect

abstract class BaseMviViewModel<I : UiIntent, S : UiState, E : UiEffect>(initialState: S) : ViewModel() {
    private val mutableState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = mutableState.asStateFlow()

    private val mutableEffect = MutableSharedFlow<E>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val uiEffect: SharedFlow<E> = mutableEffect.asSharedFlow()

    abstract fun onEvent(intent: I)

    protected fun setState(reducer: S.() -> S) = mutableState.update { it.reducer() }
    protected fun sendEffect(effect: E) { mutableEffect.tryEmit(effect) }
}
