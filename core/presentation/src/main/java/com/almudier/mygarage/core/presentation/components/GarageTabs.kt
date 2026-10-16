package com.almudier.mygarage.core.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme

/** Scope for [GarageTabs]; add tabs with [GarageTab]. */
interface GarageTabsScope : RowScope

private class GarageTabsScopeImpl(row: RowScope) : GarageTabsScope, RowScope by row

/**
 * Segmented tabs. The container draws the track; each tab is a slot added with [GarageTab].
 * The selected tab fills with `text`, so selection reads without relying on color.
 */
@Composable
fun GarageTabs(
    modifier: Modifier = Modifier,
    content: @Composable GarageTabsScope.() -> Unit,
) {
    val shape = RoundedCornerShape(GarageDesignTheme.radius.sm)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GarageDesignTheme.colors.surface)
            .border(GarageDesignTheme.stroke.thin, GarageDesignTheme.colors.line, shape)
            .padding(3.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        GarageTabsScopeImpl(this).content()
    }
}

@Composable
fun GarageTabsScope.GarageTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    label: @Composable () -> Unit,
) {
    val colors = GarageDesignTheme.colors
    val fill by animateColorAsState(if (selected) colors.text else Color.Transparent, label = "tabFill")
    val content by animateColorAsState(if (selected) colors.background else colors.dim, label = "tabContent")
    Row(
        modifier = modifier
            .weight(1f)
            .height(34.dp)
            .clip(RoundedCornerShape(GarageDesignTheme.radius.xs))
            .background(fill)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Tab
                this.selected = selected
            },
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val style = GarageDesignTheme.typography.label.copy(color = content, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        CompositionLocalProvider(LocalContentColor provides content, LocalTextStyle provides style) {
            leading?.invoke()
            label()
        }
    }
}

/** Convenience: a text tab with an optional leading dot. */
@Composable
fun GarageTabsScope.GarageTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    dot: Color? = null,
) {
    GarageTab(selected = selected, onClick = onClick, leading = dot?.let { { GarageDot(it) } }) {
        Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun GarageTabsPreview() {
    var selected by remember { mutableIntStateOf(0) }
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        GarageTabs {
            listOf("Maint." to Color(0xFF4FD8C4), "Upgrade" to Color(0xFFFFB547), "Detailing" to Color(0xFFB39DFF))
                .forEachIndexed { i, (label, color) -> GarageTab(label, selected == i, { selected = i }, dot = color) }
        }
        GarageTabs {
            GarageTab("This year", selected = false, onClick = {})
            GarageTab("All time", selected = true, onClick = {})
        }
    }
}
