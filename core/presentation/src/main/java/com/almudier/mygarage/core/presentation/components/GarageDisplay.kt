package com.almudier.mygarage.core.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme

// ---------------------------------------------------------------- Badge

enum class GarageBadgeStyle { Tinted, Dot }

/** Small status label. Color and icon are parameters: "Due soon", "Overdue", "Synced"… */
@Composable
fun GarageBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    style: GarageBadgeStyle = GarageBadgeStyle.Tinted,
    icon: ImageVector? = null,
) {
    val textStyle = GarageDesignTheme.typography.label.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
    if (style == GarageBadgeStyle.Dot) {
        Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GarageDot(color)
            Text(text, style = textStyle.copy(color = GarageDesignTheme.colors.dim, fontWeight = FontWeight.SemiBold))
        }
        return
    }
    Row(
        modifier
            .clip(RoundedCornerShape(GarageDesignTheme.radius.xs))
            .background(color.copy(alpha = 0.13f))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
        Text(text, style = textStyle.copy(color = color))
    }
}

// ---------------------------------------------------------------- Section header

/** Small caps label that starts a section, with an optional trailing slot (count, total, action). */
@Composable
fun GarageSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val style = GarageDesignTheme.typography.eyebrow.copy(fontSize = 10.sp)
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title.uppercase(), style = style, modifier = Modifier.weight(1f))
        if (trailing != null) {
            CompositionLocalProvider(LocalTextStyle provides style, LocalContentColor provides style.color) { trailing() }
        }
    }
}

// ---------------------------------------------------------------- Progress bar

/** Thin linear progress. [progress] is clamped to 0..1. */
@Composable
fun GarageProgressBar(progress: Float, color: Color, modifier: Modifier = Modifier) {
    val track = GarageDesignTheme.colors.surface1
    val radius = GarageDesignTheme.radius.xxs
    Canvas(modifier.fillMaxWidth().height(6.dp)) {
        val r = CornerRadius(radius.toPx())
        drawRoundRect(track, cornerRadius = r)
        val w = size.width * progress.coerceIn(0f, 1f)
        if (w > 0f) drawRoundRect(color, size = Size(w, size.height), cornerRadius = r)
    }
}

// ---------------------------------------------------------------- Stacked bar + bar chart

@Immutable
data class BarSegment(val value: Float, val color: Color)

/** One horizontal bar split into segments. Non-zero segments are at least 2dp wide. */
@Composable
fun GarageStackedBar(segments: List<BarSegment>, modifier: Modifier = Modifier) {
    val track = GarageDesignTheme.colors.surface1
    val radius = GarageDesignTheme.radius.xxs
    Canvas(modifier.fillMaxWidth().height(8.dp)) {
        val r = CornerRadius(radius.toPx())
        val visible = segments.filter { it.value > 0f }
        val total = visible.sumOf { it.value.toDouble() }.toFloat()
        if (total <= 0f) {
            drawRoundRect(track, cornerRadius = r)
            return@Canvas
        }
        val gap = 2.dp.toPx()
        val minW = 2.dp.toPx()
        val available = size.width - gap * (visible.size - 1)
        val raw = visible.map { (it.value / total * available).coerceAtLeast(minW) }
        val scale = available / raw.sum()
        var x = 0f
        visible.forEachIndexed { i, segment ->
            val w = raw[i] * scale
            drawRoundRect(segment.color, topLeft = Offset(x, 0f), size = Size(w, size.height), cornerRadius = r)
            x += w + gap
        }
    }
}

/**
 * Vertical stacked columns with labels underneath. Each column lists segments bottom → top.
 * Empty columns show a thin stub. [highlightIndex] makes that label bright.
 */
@Composable
fun GarageBarChart(
    columns: List<List<BarSegment>>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    highlightIndex: Int? = null,
) {
    val stub = GarageDesignTheme.colors.line
    val radius = GarageDesignTheme.radius.xxs
    Column(modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(92.dp)) {
            if (columns.isEmpty()) return@Canvas
            val gap = 7.dp.toPx()
            val colWidth = (size.width - gap * (columns.size - 1)) / columns.size
            val maxTotal = columns.maxOf { col -> col.sumOf { it.value.toDouble() } }.toFloat().coerceAtLeast(1f)
            val r = CornerRadius(radius.toPx())
            val segGap = 2.dp.toPx()
            columns.forEachIndexed { i, col ->
                val x = i * (colWidth + gap)
                var bottom = size.height
                val nonZero = col.filter { it.value > 0f }
                if (nonZero.isEmpty()) {
                    drawRoundRect(stub, topLeft = Offset(x, size.height - 2.dp.toPx()), size = Size(colWidth, 2.dp.toPx()), cornerRadius = r)
                } else {
                    nonZero.forEach { segment ->
                        val h = (segment.value / maxTotal * size.height).coerceAtLeast(3.dp.toPx())
                        drawRoundRect(segment.color, topLeft = Offset(x, bottom - h), size = Size(colWidth, h), cornerRadius = r)
                        bottom -= h + segGap
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            labels.forEachIndexed { i, label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = GarageDesignTheme.typography.mono.copy(
                        fontSize = 9.sp,
                        color = if (i == highlightIndex) GarageDesignTheme.colors.text else GarageDesignTheme.colors.mute,
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}

// ---------------------------------------------------------------- Empty state

/** Shown when a list or chart has nothing yet. Always offer a next step through [action]. */
@Composable
fun GarageEmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    text: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = GarageDesignTheme.colors
    Column(
        modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(GarageDesignTheme.radius.sm))
                .background(colors.surface1)
                .border(GarageDesignTheme.stroke.thin, colors.line, RoundedCornerShape(GarageDesignTheme.radius.sm)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.dim, modifier = Modifier.size(28.dp))
        }
        Text(title, style = GarageDesignTheme.typography.h2.copy(fontSize = 16.sp), modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
        if (text != null) {
            Text(text, style = GarageDesignTheme.typography.body.copy(color = colors.dim, fontSize = 12.sp), textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 280.dp))
        }
        if (action != null) Box(Modifier.padding(top = 14.dp)) { action() }
    }
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun GarageDisplayPreview() {
    val m = Color(0xFF4FD8C4); val u = Color(0xFFFFB547); val d = Color(0xFFB39DFF)
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GarageBadge("Oil due in 3 weeks", GarageDesignTheme.colors.warning, icon = Icons.Rounded.Schedule)
            GarageBadge("Synced", GarageDesignTheme.colors.good, style = GarageBadgeStyle.Dot)
        }
        GarageSectionHeader("Items · 2", trailing = { Text("Add", color = GarageDesignTheme.colors.primary) })
        GarageProgressBar(0.88f, GarageDesignTheme.colors.warning)
        GarageStackedBar(listOf(BarSegment(35f, m), BarSegment(2230f, u), BarSegment(25f, d)))
        GarageBarChart(
            columns = listOf(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), listOf(BarSegment(10f, d), BarSegment(1520f, u)), listOf(BarSegment(15f, d), BarSegment(35f, m), BarSegment(710f, u))),
            labels = listOf("APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT"),
            highlightIndex = 6,
        )
        GarageEmptyState(Icons.Rounded.Search, "No records found", text = "Nothing matches \"ceramic\" in this car's detailing records.")
    }
}
