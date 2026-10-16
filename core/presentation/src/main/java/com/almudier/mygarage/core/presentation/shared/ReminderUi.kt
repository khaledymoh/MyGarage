package com.almudier.mygarage.core.presentation.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.almudier.mygarage.core.domain.model.ReminderState
import com.almudier.mygarage.core.domain.model.ReminderStatus
import com.almudier.mygarage.core.presentation.R
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme

/** "due in 3 weeks", "overdue 4 days", "not logged yet". */
@Composable
fun reminderDueText(status: ReminderStatus): String {
    val days = status.daysLeft ?: return stringResource(R.string.reminder_no_record)
    return when {
        days < 0 -> stringResource(R.string.reminder_overdue_days, -days.toInt())
        days == 0L -> stringResource(R.string.reminder_due_today)
        days < 14 -> stringResource(R.string.reminder_due_days, days.toInt())
        days < 70 -> stringResource(R.string.reminder_due_weeks, (days / 7).toInt())
        else -> stringResource(R.string.reminder_due_months, (days / 30).toInt())
    }
}

/** good → warning → danger as a reminder gets closer; mute when never logged. */
@Composable
fun reminderColor(state: ReminderState): Color {
    val colors = GarageDesignTheme.colors
    return when (state) {
        ReminderState.OnTrack -> colors.good
        ReminderState.DueSoon -> colors.warning
        ReminderState.Overdue -> colors.danger
        ReminderState.NoRecord -> colors.mute
    }
}
