package com.almudier.mygarage.core.presentation.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.almudier.mygarage.core.presentation.R

val GarageFont = FontFamily(
    fonts = listOf(
        Font(resId = R.font.inter_regular, weight = FontWeight.Normal),
        Font(resId = R.font.inter_medium, weight = FontWeight.Medium),
        Font(resId = R.font.inter_semi_bold, weight = FontWeight.SemiBold),
        Font(resId = R.font.inter_bold, weight = FontWeight.Bold),
        Font(resId = R.font.inter_black, weight = FontWeight.Black),
    )
)

/** Numbers, money, plates and VINs. System monospace until JetBrains Mono is bundled. */
val GarageMonoFont: FontFamily = FontFamily.Monospace
