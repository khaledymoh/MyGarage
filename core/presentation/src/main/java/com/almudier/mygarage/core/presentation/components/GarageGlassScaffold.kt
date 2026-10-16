package com.almudier.mygarage.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

/**
 * A screen whose sticky [topBar] and [bottomBar] are frosted glass: slightly translucent and
 * blurring whatever scrolls behind them. [content] fills the whole screen and receives padding
 * equal to the bars' heights; apply it inside the scroll container so content can pass beneath.
 * Bars should use a transparent background.
 */
@Composable
fun GarageGlassScaffold(
    modifier: Modifier = Modifier,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable BoxScope.(PaddingValues) -> Unit,
) {
    val colors = GarageDesignTheme.colors
    val density = LocalDensity.current
    val hazeState = remember { HazeState() }
    var topHeight by remember { mutableIntStateOf(0) }
    var bottomHeight by remember { mutableIntStateOf(0) }

    Box(modifier.fillMaxSize().background(colors.background)) {
        Box(Modifier.fillMaxSize().hazeSource(hazeState)) {
            val top = if (topBar != null) topHeight else 0
            val bottom = if (bottomBar != null) bottomHeight else 0
            val padding = with(density) { PaddingValues(top = top.toDp(), bottom = bottom.toDp()) }
            content(padding)
        }
        if (topBar != null) {
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .onSizeChanged { topHeight = it.height }
                    .garageGlass(hazeState),
            ) {
                topBar()
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(GarageDesignTheme.stroke.thin).background(colors.line.copy(alpha = 0.6f)))
            }
        }
        if (bottomBar != null) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .onSizeChanged { bottomHeight = it.height }
                    .garageGlass(hazeState),
            ) {
                Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().height(GarageDesignTheme.stroke.thin).background(colors.line.copy(alpha = 0.6f)))
                bottomBar()
            }
        }
    }
}

/** Frosted glass: a 50% surface tint over a 24dp blur of what is behind. */
@Composable
private fun Modifier.garageGlass(state: HazeState): Modifier {
    val colors = GarageDesignTheme.colors
    return hazeEffect(state = state) {
        backgroundColor = colors.background
        blurRadius = 24.dp
        noiseFactor = 0.04f
        tints = listOf(HazeTint(colors.surface.copy(alpha = 0.5f)))
    }
}
