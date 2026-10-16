package com.almudier.mygarage.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme

/** Height of the bottom bar, so tab content can pad its last item above it. */
val LocalMainBottomBarInset = staticCompositionLocalOf { 0.dp }

/**
 * Bottom bar: tab items from [content] (use [GarageNavItem]) with [centerAction] raised in the
 * middle slot. Items share the width equally; the center action takes one slot.
 */
@Composable
fun GarageBottomNavigation(
    modifier: Modifier = Modifier,
    centerAction: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = GarageDesignTheme.colors
    val lineWidth = GarageDesignTheme.stroke.thin
    val raise = 18.dp
    Layout(
        contents = listOf(content, { if (centerAction != null) centerAction() }),
        modifier = modifier
            .background(colors.surface.copy(alpha = 0.96f))
            .drawBehind { drawLine(colors.line, Offset(0f, 0f), Offset(size.width, 0f), lineWidth.toPx()) }
            .navigationBarsPadding()
            .padding(top = 8.dp, bottom = 10.dp, start = 6.dp, end = 6.dp),
    ) { (items, center), constraints ->
        val slots = items.size + if (center.isNotEmpty()) 1 else 0
        val slotWidth = if (slots == 0) 0 else constraints.maxWidth / slots
        val itemPlaceables = items.map { it.measure(Constraints.fixedWidth(slotWidth)) }
        val centerPlaceable = center.firstOrNull()?.measure(Constraints())
        val height = itemPlaceables.maxOfOrNull { it.height } ?: 0
        layout(constraints.maxWidth, height) {
            val half = (itemPlaceables.size + 1) / 2
            var slot = 0
            itemPlaceables.forEachIndexed { index, placeable ->
                if (index == half && centerPlaceable != null) {
                    centerPlaceable.place(
                        x = slot * slotWidth + (slotWidth - centerPlaceable.width) / 2,
                        y = (height - centerPlaceable.height) / 2 - raise.roundToPx(),
                    )
                    slot++
                }
                placeable.place(slot * slotWidth, 0)
                slot++
            }
        }
    }
}

@Composable
fun GarageNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    selectedIcon: ImageVector = icon,
) {
    val colors = GarageDesignTheme.colors
    val color = if (selected) colors.text else colors.mute
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 28.dp),
                role = Role.Tab,
                onClick = onClick,
            )
            .semantics { this.selected = selected }
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(if (selected) selectedIcon else icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Text(label, style = GarageDesignTheme.typography.caption.copy(color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold), maxLines = 1)
    }
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun GarageBottomNavigationPreview() {
    Column(Modifier.padding(top = 40.dp)) {
        GarageBottomNavigation(
            centerAction = {
                GarageIconButton(Icons.Rounded.Add, "Add record", onClick = {}, style = GarageIconButtonStyle.Filled, size = GarageIconButtonSize.Large)
            },
        ) {
            GarageNavItem(true, {}, Icons.Rounded.Speed, "Dash")
            GarageNavItem(false, {}, Icons.AutoMirrored.Rounded.ReceiptLong, "Log")
            GarageNavItem(false, {}, Icons.Rounded.Insights, "Insights")
            GarageNavItem(false, {}, Icons.Rounded.Settings, "Settings")
        }
    }
}
