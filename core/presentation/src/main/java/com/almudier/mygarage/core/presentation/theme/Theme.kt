package com.almudier.mygarage.core.presentation.theme

import android.app.Activity
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

/** Cockpit "Ice" palette. The app is dark only; both theme slots use it. */
internal val garageColorsIce = GarageColors(
    background   = Color(0xFF080B11),
    background1  = Color(0xFF06080D),
    surface      = Color(0xFF111722),
    surface1     = Color(0xFF18202D),
    surface2     = Color(0xFF202A3A),
    primary      = Color(0xFF5AA9FF),
    primaryMuted = Color(0xFF13263F),
    onPrimary    = Color(0xFF06101C),
    line         = Color(0xFF243044),
    text         = Color(0xFFEDF2FA),
    dim          = Color(0xFF93A1B7),
    mute         = Color(0xFF5A677D),
    good         = Color(0xFF4FD8C4),
    warning      = Color(0xFFFFC857),
    danger       = Color(0xFFFF7A85),
    dangerMuted  = Color(0xFF2E1519),
)

internal val garageColorsLight = garageColorsIce
internal val garageColorsDark = garageColorsIce

internal fun buildGarageTypography(colors: GarageColors) = GarageTypography(
    hero = TextStyle(
        color = colors.text, fontSize = 36.sp, letterSpacing = (-1.2).sp,
        fontFamily = GarageFont, fontWeight = FontWeight.Black,
    ),
    display = TextStyle(
        color = colors.text, fontSize = 30.sp, letterSpacing = (-0.8).sp,
        fontFamily = GarageFont, fontWeight = FontWeight.Bold, lineHeight = 36.sp,
    ),
    h1 = TextStyle(
        color = colors.text, fontSize = 24.sp, letterSpacing = (-0.5).sp,
        fontFamily = GarageFont, fontWeight = FontWeight.Bold, lineHeight = 30.sp,
    ),
    h2 = TextStyle(
        color = colors.text, fontSize = 18.sp, letterSpacing = (-0.2).sp,
        fontFamily = GarageFont, fontWeight = FontWeight.Bold, lineHeight = 24.sp,
    ),
    h3 = TextStyle(
        color = colors.text, fontSize = 15.sp, letterSpacing = (-0.1).sp,
        fontFamily = GarageFont, fontWeight = FontWeight.SemiBold, lineHeight = 21.sp,
    ),
    eyebrow = TextStyle(
        color = colors.dim, fontSize = 10.5.sp, letterSpacing = 1.2.sp,
        fontFamily = GarageFont, fontWeight = FontWeight.Bold,
    ),
    bodyLarge = TextStyle(
        color = colors.text, fontSize = 15.sp, letterSpacing = 0.sp,
        fontFamily = GarageFont, fontWeight = FontWeight.Normal, lineHeight = 22.sp,
    ),
    body = TextStyle(
        color = colors.text, fontSize = 13.sp, letterSpacing = 0.1.sp,
        fontFamily = GarageFont, fontWeight = FontWeight.Normal, lineHeight = 19.sp,
    ),
    caption = TextStyle(
        color = colors.dim, fontSize = 11.sp, letterSpacing = 0.sp,
        fontFamily = GarageFont, fontWeight = FontWeight.Medium,
    ),
    label = TextStyle(
        color = colors.text, fontSize = 13.sp, letterSpacing = 0.sp,
        fontFamily = GarageFont, fontWeight = FontWeight.SemiBold,
    ),
    numberHero = TextStyle(
        color = colors.text, fontSize = 34.sp, letterSpacing = (-1.5).sp,
        fontFamily = GarageMonoFont, fontWeight = FontWeight.Bold, lineHeight = 40.sp,
    ),
    number = TextStyle(
        color = colors.text, fontSize = 20.sp, letterSpacing = (-0.5).sp,
        fontFamily = GarageMonoFont, fontWeight = FontWeight.Bold, lineHeight = 26.sp,
    ),
    mono = TextStyle(
        color = colors.dim, fontSize = 11.sp, letterSpacing = 0.sp,
        fontFamily = GarageMonoFont, fontWeight = FontWeight.Medium, lineHeight = 15.sp,
    ),
)

internal val garageSpacing = GarageSpacing(
    xxs = 4.dp,
    xs  = 8.dp,
    sm  = 12.dp,
    nr  = 16.dp,
    md  = 20.dp,
    lr  = 24.dp,
    xl  = 32.dp,
    xxl = 48.dp,
)

/** Cockpit "Sharp" corners: small and consistent. [GarageRadius.full] is for dots only. */
internal val garageRadius = GarageRadius(
    xxs  = 2.dp,
    xs   = 4.dp,
    sm   = 6.dp,
    md   = 6.dp,
    lg   = 8.dp,
    xl   = 8.dp,
    full = 1000.dp,
    xxfull = 1000.dp,
)

internal val garageStroke = GarageStroke(
    thin   = 1.dp,
    medium = 1.5.dp,
    thick  = 2.5.dp,
)

@Composable
fun GarageTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) garageColorsDark else garageColorsLight
    val typography = buildGarageTypography(colors)
    val ripple = ripple()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    CompositionLocalProvider(
        LocalGarageColors provides colors,
        LocalGarageTypography provides typography,
        LocalGarageSpacing provides garageSpacing,
        LocalGarageRadius provides garageRadius,
        LocalGarageStroke provides garageStroke,
        LocalIndication provides ripple,
        content = content,
    )
}

object GarageDesignTheme {
    val colors: GarageColors
        @Composable get() = LocalGarageColors.current

    val typography: GarageTypography
        @Composable get() = LocalGarageTypography.current

    val spacing: GarageSpacing
        @Composable get() = LocalGarageSpacing.current

    val radius: GarageRadius
        @Composable get() = LocalGarageRadius.current

    val stroke: GarageStroke
        @Composable get() = LocalGarageStroke.current
}