package com.almudier.mygarage.core.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

@Immutable
data class GarageSpacing(
    val xxs: Dp,
    val xs: Dp,
    val sm: Dp,
    val nr: Dp,
    val md: Dp,
    val lr: Dp,
    val xl: Dp,
    val xxl: Dp,
)

@Immutable
data class GarageRadius(
    val xxs: Dp,
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
    val full: Dp,
    val xxfull: Dp,
)

@Immutable
data class GarageStroke(
    val thin: Dp,
    val medium: Dp,
    val thick: Dp,
)

val LocalGarageSpacing = staticCompositionLocalOf {
    GarageSpacing(
        xxs = Dp.Unspecified,
        xs = Dp.Unspecified,
        sm = Dp.Unspecified,
        nr = Dp.Unspecified,
        md = Dp.Unspecified,
        lr = Dp.Unspecified,
        xl = Dp.Unspecified,
        xxl = Dp.Unspecified,
    )
}

val LocalGarageRadius = staticCompositionLocalOf {
    GarageRadius(
        xxs = Dp.Unspecified,
        xs = Dp.Unspecified,
        sm = Dp.Unspecified,
        md = Dp.Unspecified,
        lg = Dp.Unspecified,
        xl = Dp.Unspecified,
        full = Dp.Unspecified,
        xxfull = Dp.Unspecified,
    )
}

val LocalGarageStroke = staticCompositionLocalOf {
    GarageStroke(
        thin = Dp.Unspecified,
        medium = Dp.Unspecified,
        thick = Dp.Unspecified,
    )
}
