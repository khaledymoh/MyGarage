package com.almudier.mygarage.core.presentation.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.almudier.mygarage.core.domain.model.MaintenanceItem
import com.almudier.mygarage.core.domain.model.MaintenanceRecord
import com.almudier.mygarage.core.domain.model.RecordType
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.R
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.components.GarageListGroup
import com.almudier.mygarage.core.presentation.components.GarageListItem
import com.almudier.mygarage.core.presentation.formatNumber
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val shortDate = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH)

/** "12 OCT" from an ISO date; the raw value if it can't be parsed. */
fun formatShortDate(isoDate: String): String =
    runCatching { LocalDate.parse(isoDate).format(shortDate).uppercase() }.getOrDefault(isoDate)

/** First item's name, plus "+N" when the record has more items. */
@Composable
fun recordTitle(record: MaintenanceRecord): String {
    val first = record.items.firstOrNull()?.service?.takeIf { it.isNotBlank() }
        ?: record.workshop.ifBlank { stringResource(record.type.label) }
    val more = record.items.size - 1
    return if (more > 0) stringResource(R.string.record_more_items, first, more) else first
}

/** "12 OCT · 48,600 KM" for maintenance with mileage, otherwise "12 OCT · UPGRADE". */
@Composable
fun recordMeta(record: MaintenanceRecord): String {
    val date = formatShortDate(record.date)
    val second = if (record.type == RecordType.Maintenance && record.mileage > 0) {
        "${formatNumber(record.mileage)} KM"
    } else {
        stringResource(record.type.label).uppercase()
    }
    return "$date · $second"
}

/** A Log row: type strip, title, mono meta line, amount with currency. */
@Composable
fun RecordRow(
    record: MaintenanceRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlight: String? = null,
) {
    GarageListItem(
        modifier = modifier,
        onClick = onClick,
        leading = { TypeStrip(record.type) },
        headline = {
            Text(highlighted(recordTitle(record), highlight), maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supporting = { Text(recordMeta(record), style = GarageDesignTheme.typography.mono) },
        trailing = { Amount(record.totalCost ?: 0.0) },
    )
}

/** 4×32dp vertical strip in the record type's color. */
@Composable
fun TypeStrip(type: RecordType, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(width = 4.dp, height = 32.dp)
            .clip(RoundedCornerShape(GarageDesignTheme.radius.xxs))
            .background(type.color),
    )
}

@Composable
private fun highlighted(text: String, query: String?): AnnotatedString {
    val term = query?.trim().orEmpty()
    val start = if (term.isEmpty()) -1 else text.indexOf(term, ignoreCase = true)
    if (start < 0) return AnnotatedString(text)
    val mark = RecordType.Upgrade.color.copy(alpha = 0.25f)
    return buildAnnotatedString {
        append(text.substring(0, start))
        withStyle(SpanStyle(background = mark)) { append(text.substring(start, start + term.length)) }
        append(text.substring(start + term.length))
    }
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun RecordRowPreview() {
    Column {
        GarageListGroup {
            RecordRow(
                MaintenanceRecord("1", "v", "2026-10-12", 48600, "Bavaria Motors", 35.0, items = listOf(MaintenanceItem("Brake fluid", 20.0, 15.0))),
                onClick = {}, highlight = "brake",
            )
            RecordRow(
                MaintenanceRecord("2", "v", "2026-10-12", 0, "Tune Lab", 650.0, items = listOf(MaintenanceItem("Valvetronic catback exhaust", 560.0, 90.0)), type = RecordType.Upgrade),
                onClick = {},
            )
            RecordRow(
                MaintenanceRecord("3", "v", "2026-09-27", 0, "", 5.0, items = listOf(MaintenanceItem("Car wash", 5.0)), type = RecordType.Detailing),
                onClick = {},
            )
        }
    }
}
