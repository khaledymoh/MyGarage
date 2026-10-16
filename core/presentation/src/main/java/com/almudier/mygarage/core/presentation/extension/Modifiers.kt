package com.almudier.mygarage.core.presentation.extension

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager

fun Modifier.clearFocusOnKeyboardDismiss(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
    var lastImeState by remember { mutableStateOf(isImeVisible) }

    LaunchedEffect(isImeVisible) {
        if (lastImeState && !isImeVisible) {
            focusManager.clearFocus()
        }
        lastImeState = isImeVisible
    }
    this
}
