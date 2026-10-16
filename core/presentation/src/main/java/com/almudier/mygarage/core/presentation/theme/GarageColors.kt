package com.almudier.mygarage.core.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class GarageColors(
    val background: Color,
    val background1: Color,
    val surface: Color,
    val surface1: Color,
    /** Pressed state on surfaces. */
    val surface2: Color,
    val primary: Color,
    val primaryMuted: Color,
    /** Text and icons drawn on [primary]. */
    val onPrimary: Color,
    val line: Color,
    val text: Color,
    val dim: Color,
    val mute: Color,
    val good: Color,
    /** Due-soon reminders. */
    val warning: Color,
    val danger: Color,
    val dangerMuted: Color,
)

val LocalGarageColors = compositionLocalOf {
    GarageColors(
        background = Color.Unspecified,
        background1 = Color.Unspecified,
        surface = Color.Unspecified,
        surface1 = Color.Unspecified,
        surface2 = Color.Unspecified,
        primary = Color.Unspecified,
        primaryMuted = Color.Unspecified,
        onPrimary = Color.Unspecified,
        line = Color.Unspecified,
        text = Color.Unspecified,
        dim = Color.Unspecified,
        mute = Color.Unspecified,
        good = Color.Unspecified,
        warning = Color.Unspecified,
        danger = Color.Unspecified,
        dangerMuted = Color.Unspecified,
    )
}
