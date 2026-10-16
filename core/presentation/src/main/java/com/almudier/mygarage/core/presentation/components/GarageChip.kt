package com.almudier.mygarage.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme

/** How a [GarageChip] looks when selected. */
@Immutable
sealed interface GarageChipStyle {
    /** Selected = inverted (text-on-light). Used for list filters. */
    data object Filter : GarageChipStyle

    /** Selected = tinted with [color]. Used for presets and multi-select filters. */
    data class Tinted(val color: Color) : GarageChipStyle
}

@Composable
fun GarageChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: GarageChipStyle = GarageChipStyle.Filter,
    leading: (@Composable () -> Unit)? = null,
    label: @Composable () -> Unit,
) {
    val colors = GarageDesignTheme.colors
    val shape = RoundedCornerShape(GarageDesignTheme.radius.xs)
    val (fill, border, content) = when {
        !selected -> Triple(colors.surface, colors.line, colors.dim)
        style is GarageChipStyle.Tinted -> Triple(style.color.copy(alpha = 0.14f), style.color.copy(alpha = 0.55f), style.color)
        else -> Triple(colors.text, colors.text, colors.background)
    }
    Row(
        modifier = modifier
            .height(32.dp)
            .clip(shape)
            .background(fill)
            .border(GarageDesignTheme.stroke.thin, border, shape)
            .clickable(role = Role.Checkbox, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(horizontal = GarageDesignTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val textStyle = GarageDesignTheme.typography.label.copy(color = content, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        CompositionLocalProvider(LocalContentColor provides content, LocalTextStyle provides textStyle) {
            leading?.invoke()
            label()
        }
    }
}

/** Convenience overload: text label with an optional leading dot or icon. */
@Composable
fun GarageChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: GarageChipStyle = GarageChipStyle.Filter,
    dot: Color? = null,
    icon: ImageVector? = null,
) {
    GarageChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        style = style,
        leading = when {
            dot != null -> ({ GarageDot(dot) })
            icon != null -> ({ Icon(icon, contentDescription = null, Modifier.size(15.dp)) })
            else -> null
        },
    ) { Text(text, maxLines = 1) }
}

/** A small colored dot, e.g. a record-type marker in chips, tabs and legends. */
@Composable
fun GarageDot(color: Color, modifier: Modifier = Modifier, size: Dp = 7.dp) {
    Box(modifier.size(size).clip(CircleShape).background(color))
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun GarageChipPreview() {
    FlowRow(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        GarageChip("All 13", selected = true, onClick = {})
        GarageChip("Maint.", selected = false, onClick = {}, dot = Color(0xFF4FD8C4))
        GarageChip("Filters", selected = false, onClick = {}, icon = Icons.Rounded.Tune)
        GarageChip("Car wash", selected = true, onClick = {}, style = GarageChipStyle.Tinted(Color(0xFFB39DFF)))
        GarageChip("Interior clean", selected = false, onClick = {}, style = GarageChipStyle.Tinted(Color(0xFFB39DFF)))
    }
}
