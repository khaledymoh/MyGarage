package com.almudier.mygarage.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.almudier.mygarage.MainActivity
import com.almudier.mygarage.R
import com.almudier.mygarage.core.domain.model.RecordType
import com.almudier.mygarage.core.domain.model.ReminderState
import com.almudier.mygarage.core.domain.model.ReminderStatus
import com.almudier.mygarage.core.presentation.shared.formatAmount
import com.almudier.mygarage.core.presentation.shared.formatMoney
import java.time.format.DateTimeFormatter

/**
 * Home-screen widget: total spent and service reminders for the picked car. One resizable
 * widget whose layout follows its size: small (2x2), medium (4x2) and large (4x4).
 */
class GarageWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = loadGarageWidgetState(context)
        provideContent { GarageWidgetContent(state) }
    }
}

class GarageWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GarageWidget()
}

// Cockpit (Ice) tokens. Widgets render outside Compose UI, so the theme can't be read here.
private object W {
    val text = Color(0xFFEDF2FA)
    val dim = Color(0xFF93A1B7)
    val mute = Color(0xFF5A677D)
    val line = Color(0xFF243044)
    val track = Color(0xFF202A3A)
    val good = Color(0xFF4FD8C4)
    val warning = Color(0xFFFFC857)
    val danger = Color(0xFFFF7A85)
    val dangerMuted = Color(0x24FF7A85)
    val maintenance = Color(0xFF4FD8C4)
    val upgrade = Color(0xFFFFB547)
    val detailing = Color(0xFFB39DFF)
    val padding = 14.dp
}

private fun color(value: Color): ColorProvider = androidx.glance.color.ColorProvider(day = value, night = value)

private fun RecordType.color(): Color = when (this) {
    RecordType.Maintenance -> W.maintenance
    RecordType.Upgrade -> W.upgrade
    RecordType.Detailing -> W.detailing
}

private fun ReminderState.color(): Color = when (this) {
    ReminderState.Overdue -> W.danger
    ReminderState.DueSoon -> W.warning
    ReminderState.OnTrack -> W.good
    ReminderState.NoRecord -> W.mute
}

@Composable
private fun GarageWidgetContent(state: GarageWidgetState) {
    val size = LocalSize.current
    val wide = size.width >= 230.dp
    val tall = size.height >= 230.dp
    Box(
        GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(ImageProvider(R.drawable.widget_background))
            .cornerRadius(R.dimen.widget_radius)
            .padding(W.padding)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        when {
            state !is GarageWidgetState.Ready -> NoCarContent()
            wide && tall -> LargeContent(state, size.width - W.padding * 2, maxReminders = if (size.height >= 400.dp) 5 else 4)
            wide -> MediumContent(state, size.width - W.padding * 2)
            else -> SmallContent(state, size.width - W.padding * 2)
        }
    }
}

// ------------------------------------------------------------------ layouts

@Composable
private fun SmallContent(state: GarageWidgetState.Ready, width: Dp) {
    Column(GlanceModifier.fillMaxSize()) {
        Header(state.carName)
        Spacer(GlanceModifier.defaultWeight())
        TotalBlock(state, numberSize = 26.sp)
        Spacer(GlanceModifier.height(8.dp))
        SpendBar(state.byType, width)
        Spacer(GlanceModifier.height(8.dp))
        UrgentPill(state.reminders.firstOrNull())
    }
}

@Composable
private fun MediumContent(state: GarageWidgetState.Ready, width: Dp) {
    val gap = 14.dp
    val half = (width - gap * 2 - 1.dp) / 2
    Row(GlanceModifier.fillMaxSize()) {
        Column(GlanceModifier.width(half).fillMaxHeight()) {
            Header(state.carName)
            Spacer(GlanceModifier.defaultWeight())
            TotalBlock(state, numberSize = 25.sp)
            Spacer(GlanceModifier.height(8.dp))
            SpendBar(state.byType, half)
            Spacer(GlanceModifier.height(7.dp))
            ThisMonthLine(state)
        }
        Spacer(GlanceModifier.width(gap))
        Box(GlanceModifier.width(1.dp).fillMaxHeight().background(W.line)) {}
        Spacer(GlanceModifier.width(gap))
        Column(GlanceModifier.defaultWeight().fillMaxHeight()) {
            Label(LocalContext.current.getString(R.string.widget_reminders))
            ReminderList(state.reminders, max = 3)
        }
    }
}

@Composable
private fun LargeContent(state: GarageWidgetState.Ready, width: Dp, maxReminders: Int) {
    val context = LocalContext.current
    Column(GlanceModifier.fillMaxSize()) {
        Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(GlanceModifier.defaultWeight()) { Header(state.carName) }
            state.since?.let { since ->
                Text(
                    context.getString(R.string.widget_since, since.format(DateTimeFormatter.ofPattern("MMM yyyy"))),
                    style = mono(10.sp, W.mute),
                    maxLines = 1,
                )
            }
        }
        Spacer(GlanceModifier.height(12.dp))
        Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Box(GlanceModifier.defaultWeight()) { TotalBlock(state, numberSize = 30.sp) }
            Column(horizontalAlignment = Alignment.End) {
                Label(context.getString(R.string.widget_this_month))
                Spacer(GlanceModifier.height(4.dp))
                Money(state.thisMonth, state.currency, 15.sp)
            }
        }
        Spacer(GlanceModifier.height(12.dp))
        // Glance drops anything past 10 children in a Row/Column, so the rest is grouped.
        Column(GlanceModifier.fillMaxWidth()) {
            SpendBar(state.byType, width)
            Spacer(GlanceModifier.height(8.dp))
            Legend(state.byType)
            Spacer(GlanceModifier.height(12.dp))
            Box(GlanceModifier.fillMaxWidth().height(1.dp).background(W.line)) {}
        }
        Column(GlanceModifier.fillMaxWidth().defaultWeight()) {
            ReminderList(state.reminders, max = maxReminders)
        }
    }
}

@Composable
private fun NoCarContent() {
    Column(GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(ImageProvider(R.mipmap.ic_launcher_round), contentDescription = null, modifier = GlanceModifier.size(40.dp))
        Spacer(GlanceModifier.height(10.dp))
        Text(
            LocalContext.current.getString(R.string.widget_no_car),
            style = TextStyle(color = color(W.dim), fontSize = 12.sp, textAlign = TextAlign.Center),
        )
    }
}

// ------------------------------------------------------------------ pieces

@Composable
private fun Header(carName: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(ImageProvider(R.mipmap.ic_launcher_round), contentDescription = null, modifier = GlanceModifier.size(18.dp))
        Spacer(GlanceModifier.width(7.dp))
        Text(carName, style = TextStyle(color = color(W.text), fontSize = 12.sp, fontWeight = FontWeight.Medium), maxLines = 1)
    }
}

@Composable
private fun Label(text: String) {
    Text(text.uppercase(), style = mono(9.sp, W.mute, bold = true), maxLines = 1)
}

@Composable
private fun TotalBlock(state: GarageWidgetState.Ready, numberSize: TextUnit) {
    Column {
        Label(LocalContext.current.getString(R.string.widget_total_spent))
        Spacer(GlanceModifier.height(3.dp))
        Money(state.total, state.currency, numberSize)
    }
}

/** "4,820 JOD": bold mono number, small dim currency code. */
@Composable
private fun Money(value: Double, currency: String, numberSize: TextUnit) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(formatAmount(value), style = mono(numberSize, W.text, bold = true), maxLines = 1)
        Spacer(GlanceModifier.width(4.dp))
        Text(
            currency,
            style = mono((numberSize.value * 0.42f).coerceAtLeast(10f).sp, W.dim),
            maxLines = 1,
            modifier = GlanceModifier.padding(bottom = (numberSize.value * 0.1f).dp),
        )
    }
}

@Composable
private fun ThisMonthLine(state: GarageWidgetState.Ready) {
    val context = LocalContext.current
    Row {
        Text(context.getString(R.string.widget_this_month) + " ", style = TextStyle(color = color(W.dim), fontSize = 10.sp), maxLines = 1)
        Text(formatMoney(state.thisMonth, state.currency), style = mono(10.sp, W.text, bold = true), maxLines = 1)
    }
}

/** Spend split by record type as one bar; segment widths follow the amounts. */
@Composable
private fun SpendBar(byType: Map<RecordType, Double>, width: Dp) {
    val parts = RecordType.entries.map { it to (byType[it] ?: 0.0) }.filter { it.second > 0 }
    val total = parts.sumOf { it.second }
    if (total <= 0) {
        Box(GlanceModifier.width(width).height(6.dp).cornerRadius(3.dp).background(W.track)) {}
        return
    }
    val gap = 2.dp
    val usable = width - gap * (parts.size - 1)
    Row(GlanceModifier.width(width).height(6.dp).cornerRadius(3.dp)) {
        parts.forEachIndexed { index, (type, amount) ->
            if (index > 0) Spacer(GlanceModifier.width(gap))
            val segment = (usable * (amount / total).toFloat()).coerceAtLeast(3.dp)
            Box(GlanceModifier.width(segment).height(6.dp).background(type.color())) {}
        }
    }
}

@Composable
private fun Legend(byType: Map<RecordType, Double>) {
    val context = LocalContext.current
    Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RecordType.entries.forEachIndexed { index, type ->
            if (index > 0) Spacer(GlanceModifier.width(10.dp))
            val label = when (type) {
                RecordType.Maintenance -> R.string.widget_type_maintenance
                RecordType.Upgrade -> R.string.widget_type_upgrade
                RecordType.Detailing -> R.string.widget_type_detailing
            }
            // one Row per entry keeps the outer Row under Glance's 10-children limit
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(GlanceModifier.size(7.dp).cornerRadius(2.dp).background(type.color())) {}
                Spacer(GlanceModifier.width(5.dp))
                Text(context.getString(label) + " ", style = TextStyle(color = color(W.dim), fontSize = 10.sp), maxLines = 1)
                Text(formatAmount(byType[type] ?: 0.0), style = mono(10.sp, W.text, bold = true), maxLines = 1)
            }
        }
    }
}

/** Reminders from the top, each with its interval progress. */
@Composable
private fun ColumnScope.ReminderList(reminders: List<ReminderStatus>, max: Int) {
    val context = LocalContext.current
    if (reminders.isEmpty()) {
        Spacer(GlanceModifier.defaultWeight())
        Text(context.getString(R.string.widget_no_reminders), style = TextStyle(color = color(W.dim), fontSize = 12.sp))
        Spacer(GlanceModifier.defaultWeight())
        return
    }
    reminders.take(max).forEach { reminder -> ReminderRow(reminder) }
    Spacer(GlanceModifier.defaultWeight())
}

@Composable
private fun ReminderRow(reminder: ReminderStatus) {
    val tint = reminder.state.color()
    Column(GlanceModifier.fillMaxWidth().padding(top = 10.dp)) {
        Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                reminder.interval.name,
                style = TextStyle(color = color(W.text), fontSize = 12.sp, fontWeight = FontWeight.Medium),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight(),
            )
            Spacer(GlanceModifier.width(8.dp))
            Text(dueText(reminder), style = mono(11.sp, tint, bold = true), maxLines = 1)
        }
        Spacer(GlanceModifier.height(4.dp))
        LinearProgressIndicator(
            progress = reminder.elapsed,
            modifier = GlanceModifier.fillMaxWidth().height(3.dp).cornerRadius(2.dp),
            color = color(tint),
            backgroundColor = color(W.track),
        )
    }
}

/** The single most urgent reminder, as a coloured pill; "all on track" when nothing is close. */
@Composable
private fun UrgentPill(reminder: ReminderStatus?) {
    val context = LocalContext.current
    val urgent = reminder?.takeIf { it.state == ReminderState.Overdue || it.state == ReminderState.DueSoon }
    val tint = urgent?.state?.color() ?: W.good
    val text = if (urgent != null) {
        context.getString(R.string.widget_pill, urgent.interval.name, dueText(urgent))
    } else {
        context.getString(if (reminder == null) R.string.widget_no_reminders else R.string.widget_all_on_track)
    }
    val background = when (urgent?.state) {
        ReminderState.Overdue -> W.dangerMuted
        ReminderState.DueSoon -> Color(0x24FFC857)
        else -> Color(0x1F4FD8C4)
    }
    Row(
        GlanceModifier.fillMaxWidth().cornerRadius(8.dp).background(background).padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(GlanceModifier.size(6.dp).cornerRadius(3.dp).background(tint)) {}
        Spacer(GlanceModifier.width(6.dp))
        Text(text, style = TextStyle(color = color(tint), fontSize = 11.sp, fontWeight = FontWeight.Medium), maxLines = 1)
    }
}

/** "6d late", "18d", "9 mo", "today", "not logged". */
@Composable
private fun dueText(reminder: ReminderStatus): String {
    val context = LocalContext.current
    val days = reminder.daysLeft ?: return context.getString(R.string.widget_due_none)
    return when {
        days < 0 -> context.getString(R.string.widget_due_late, -days.toInt())
        days == 0L -> context.getString(R.string.widget_due_today)
        days < 70 -> context.getString(R.string.widget_due_days, days.toInt())
        else -> context.getString(R.string.widget_due_months, (days / 30).toInt())
    }
}

private fun mono(size: TextUnit, tint: Color, bold: Boolean = false) = TextStyle(
    color = color(tint),
    fontSize = size,
    fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
    fontFamily = FontFamily.Monospace,
)
