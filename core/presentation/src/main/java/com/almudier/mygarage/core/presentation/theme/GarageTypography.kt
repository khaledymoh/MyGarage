package com.almudier.mygarage.core.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle

@Immutable
data class GarageTypography(
    val hero: TextStyle,
    val display: TextStyle,
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val eyebrow: TextStyle,
    val bodyLarge: TextStyle,
    val body: TextStyle,
    val caption: TextStyle,
    val label: TextStyle,
    /** Big money totals (34sp mono). */
    val numberHero: TextStyle,
    /** Stat values such as mileage or a month's spend (20sp mono). */
    val number: TextStyle,
    /** Meta lines: dates, plates, VINs, small amounts (11sp mono). */
    val mono: TextStyle,
)

val LocalGarageTypography = compositionLocalOf {
    GarageTypography(
        hero = TextStyle.Default,
        display = TextStyle.Default,
        h1 = TextStyle.Default,
        h2 = TextStyle.Default,
        h3 = TextStyle.Default,
        eyebrow = TextStyle.Default,
        bodyLarge = TextStyle.Default,
        body = TextStyle.Default,
        caption = TextStyle.Default,
        label = TextStyle.Default,
        numberHero = TextStyle.Default,
        number = TextStyle.Default,
        mono = TextStyle.Default,
    )
}
