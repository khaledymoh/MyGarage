package com.almudier.mygarage.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme

enum class GarageTone { Default, Danger }

/**
 * A row with four slots: leading · headline · supporting · trailing.
 * Settings rows, key/value facts and legend lines are all this primitive filled differently.
 */
@Composable
fun GarageListItem(
    headline: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    supporting: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    tone: GarageTone = GarageTone.Default,
    dense: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = GarageDesignTheme.colors
    val typography = GarageDesignTheme.typography
    val headlineColor = if (tone == GarageTone.Danger) colors.danger else colors.text
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = if (dense) 36.dp else 52.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = GarageDesignTheme.spacing.sm, vertical = if (dense) 6.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GarageDesignTheme.spacing.sm),
    ) {
        if (leading != null) {
            CompositionLocalProvider(LocalContentColor provides if (tone == GarageTone.Danger) colors.danger else colors.dim) {
                leading()
            }
        }
        Column(Modifier.weight(1f)) {
            CompositionLocalProvider(
                LocalContentColor provides headlineColor,
                LocalTextStyle provides typography.label.copy(color = headlineColor),
            ) { headline() }
            if (supporting != null) {
                CompositionLocalProvider(
                    LocalContentColor provides colors.dim,
                    LocalTextStyle provides typography.caption.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Normal),
                ) { supporting() }
            }
        }
        if (trailing != null) {
            CompositionLocalProvider(
                LocalContentColor provides colors.dim,
                LocalTextStyle provides typography.mono.copy(fontSize = 12.sp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) { trailing() }
            }
        }
    }
}

/** Convention leading tile for list rows: a 32dp icon tile. */
@Composable
fun GarageListIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    background: Color = GarageDesignTheme.colors.surface1,
) {
    Box(
        modifier
            .size(32.dp)
            .clip(RoundedCornerShape(GarageDesignTheme.radius.xs))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
    }
}

/** Trailing chevron for rows that navigate. */
@Composable
fun GarageChevron() {
    Icon(
        Icons.AutoMirrored.Rounded.KeyboardArrowRight,
        contentDescription = null,
        tint = GarageDesignTheme.colors.mute,
        modifier = Modifier.size(20.dp),
    )
}

private class DividerPositions { var ys: List<Float> = emptyList() }

/** Wraps rows in a card and draws a 1dp divider between consecutive children. */
@Composable
fun GarageListGroup(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = GarageDesignTheme.colors
    val stroke = GarageDesignTheme.stroke.thin
    val shape = RoundedCornerShape(GarageDesignTheme.radius.sm)
    val dividers = remember { DividerPositions() }
    Layout(
        content = content,
        modifier = modifier
            .clip(shape)
            .background(colors.surface)
            .border(stroke, colors.line, shape)
            .drawBehind {
                val px = stroke.toPx()
                dividers.ys.forEach { y -> drawLine(colors.line, Offset(0f, y), Offset(size.width, y), px) }
            },
    ) { measurables, constraints ->
        val childConstraints = constraints.copy(minHeight = 0)
        val placeables = measurables.map { it.measure(childConstraints) }
        val gap = stroke.roundToPx().coerceAtLeast(1)
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else placeables.maxOfOrNull { it.width } ?: 0
        val height = placeables.sumOf { it.height } + gap * (placeables.size - 1).coerceAtLeast(0)
        layout(width, height.coerceAtLeast(constraints.minHeight)) {
            var y = 0
            val ys = ArrayList<Float>(placeables.size)
            placeables.forEachIndexed { index, placeable ->
                if (index > 0) {
                    ys += y + gap / 2f
                    y += gap
                }
                placeable.place(0, y)
                y += placeable.height
            }
            dividers.ys = ys
        }
    }
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun GarageListItemPreview() {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GarageListGroup {
            GarageListItem(
                headline = { Text("Appearance") },
                leading = { GarageListIcon(Icons.Rounded.DarkMode) },
                trailing = { Text("Dark"); GarageChevron() },
                onClick = {},
            )
            GarageListItem(
                headline = { Text("Export data") },
                supporting = { Text("JSON backup") },
                leading = { GarageListIcon(Icons.Rounded.Download) },
                trailing = { GarageChevron() },
                onClick = {},
            )
            GarageListItem(
                headline = { Text("Sign out") },
                leading = { GarageListIcon(Icons.AutoMirrored.Rounded.Logout, background = GarageDesignTheme.colors.dangerMuted) },
                tone = GarageTone.Danger,
                onClick = {},
            )
        }
        GarageListGroup {
            GarageListItem(headline = { Text("Mileage", color = GarageDesignTheme.colors.dim) }, trailing = { Text("48,600 km", color = GarageDesignTheme.colors.text) }, dense = true)
            GarageListItem(headline = { Text("Workshop", color = GarageDesignTheme.colors.dim) }, trailing = { Text("Bavaria Motors", color = GarageDesignTheme.colors.text) }, dense = true)
        }
    }
}
