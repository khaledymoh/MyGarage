package com.almudier.mygarage.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme

enum class GarageCardStyle {
    /** Default surface. */
    Filled,

    /** A nested surface inside another card. */
    Raised,

    /** A placeholder for adding something (attach a receipt, add another item). */
    Dashed,
}

/** The base surface. It adds only fill, border, corner and padding; [content] is a slot. */
@Composable
fun GarageCard(
    modifier: Modifier = Modifier,
    style: GarageCardStyle = GarageCardStyle.Filled,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = GarageDesignTheme.colors
    val radius = GarageDesignTheme.radius.sm
    val shape = RoundedCornerShape(radius)
    val fill = when (style) {
        GarageCardStyle.Filled -> colors.surface
        GarageCardStyle.Raised -> colors.surface1
        GarageCardStyle.Dashed -> Color.Transparent
    }
    val borderColor = if (selected) colors.primary.copy(alpha = 0.5f) else colors.line
    Column(
        modifier = modifier
            .clip(shape)
            .background(fill)
            .then(
                if (style == GarageCardStyle.Dashed) {
                    Modifier.dashedBorder(GarageDesignTheme.stroke.medium, borderColor, radius)
                } else {
                    Modifier.border(GarageDesignTheme.stroke.thin, borderColor, shape)
                }
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        verticalArrangement = verticalArrangement,
        content = content,
    )
}

fun Modifier.dashedBorder(width: Dp, color: Color, cornerRadius: Dp): Modifier = drawBehind {
    val strokePx = width.toPx()
    drawRoundRect(
        color = color,
        topLeft = androidx.compose.ui.geometry.Offset(strokePx / 2, strokePx / 2),
        size = androidx.compose.ui.geometry.Size(size.width - strokePx, size.height - strokePx),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(width = strokePx, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
    )
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun GarageCardPreview() {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        GarageCard(Modifier.fillMaxWidth()) {
            Text("Filled card", style = GarageDesignTheme.typography.h3)
            GarageCard(Modifier.fillMaxWidth().padding(top = 8.dp), style = GarageCardStyle.Raised) {
                Text("Raised, nested", style = GarageDesignTheme.typography.body)
            }
        }
        GarageCard(Modifier.fillMaxWidth(), selected = true) { Text("Selected", style = GarageDesignTheme.typography.h3) }
        GarageCard(Modifier.fillMaxWidth(), style = GarageCardStyle.Dashed, onClick = {}) {
            Text("＋ Add another item", style = GarageDesignTheme.typography.label, color = GarageDesignTheme.colors.dim)
        }
    }
}
