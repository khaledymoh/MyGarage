package com.almudier.mygarage.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Garage
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.almudier.mygarage.core.domain.model.DefaultServiceIntervals
import com.almudier.mygarage.core.domain.model.ServiceInterval
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.components.GarageBottomSheet
import com.almudier.mygarage.core.presentation.components.GarageChevron
import com.almudier.mygarage.core.presentation.components.GarageDialog
import com.almudier.mygarage.core.presentation.components.GarageDialogButton
import com.almudier.mygarage.core.presentation.components.GarageIconButton
import com.almudier.mygarage.core.presentation.components.GarageListGroup
import com.almudier.mygarage.core.presentation.components.GarageListIcon
import com.almudier.mygarage.core.presentation.components.GarageListItem
import com.almudier.mygarage.core.presentation.components.GarageSectionHeader
import com.almudier.mygarage.core.presentation.components.GarageTone
import com.almudier.mygarage.core.presentation.components.LocalMainBottomBarInset
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme

private val Currencies = listOf("JOD", "SAR", "AED", "USD", "EUR", "GBP")

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreenContent(
        dialog = state.dialog,
        sheet = state.sheet,
        currency = state.currency,
        intervals = state.intervals,
        signingOut = state.signingOut,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun SettingsScreenContent(
    dialog: SettingsContract.Dialog?,
    sheet: SettingsContract.Sheet?,
    currency: String,
    intervals: List<ServiceInterval>,
    onEvent: (SettingsContract.Intent) -> Unit,
    signingOut: Boolean = false,
) {
    val colors = GarageDesignTheme.colors
    val spacing = GarageDesignTheme.spacing
    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.nr)
            .padding(bottom = LocalMainBottomBarInset.current + spacing.xl),
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        Text(stringResource(R.string.settings_title), style = GarageDesignTheme.typography.h1, modifier = Modifier.padding(top = spacing.sm, bottom = spacing.xs))

        Section(R.string.settings_reminders)
        GarageListGroup(Modifier.fillMaxWidth()) {
            GarageListItem(
                leading = { GarageListIcon(Icons.Rounded.Schedule) },
                headline = { Text(stringResource(R.string.settings_intervals)) },
                supporting = { Text(stringResource(R.string.settings_intervals_hint)) },
                trailing = { Text(pluralStringResource(R.plurals.settings_intervals_count, intervals.size, intervals.size)); GarageChevron() },
                onClick = { onEvent(SettingsContract.Intent.OpenSheet(SettingsContract.Sheet.INTERVALS)) },
            )
        }

        Section(R.string.settings_units)
        GarageListGroup(Modifier.fillMaxWidth()) {
            GarageListItem(
                leading = { GarageListIcon(Icons.Rounded.Payments) },
                headline = { Text(stringResource(R.string.settings_currency)) },
                trailing = { Text(currency); GarageChevron() },
                onClick = { onEvent(SettingsContract.Intent.OpenSheet(SettingsContract.Sheet.CURRENCY)) },
            )
            GarageListItem(
                leading = { GarageListIcon(Icons.Rounded.Straighten) },
                headline = { Text(stringResource(R.string.settings_distance)) },
                trailing = { Text(stringResource(R.string.settings_km)) },
            )
            GarageListItem(
                leading = { GarageListIcon(Icons.Rounded.CalendarMonth) },
                headline = { Text(stringResource(R.string.settings_date_format)) },
                trailing = { Text("12 Oct 2026") },
            )
            GarageListItem(
                leading = { GarageListIcon(Icons.Rounded.DarkMode) },
                headline = { Text(stringResource(R.string.settings_appearance)) },
                trailing = { Text(stringResource(R.string.settings_dark)) },
            )
        }

        Section(R.string.settings_data)
        GarageListGroup(Modifier.fillMaxWidth()) {
            GarageListItem(
                leading = { GarageListIcon(Icons.Rounded.Download) },
                headline = { Text(stringResource(R.string.settings_export)) },
                supporting = { Text(stringResource(R.string.settings_coming_soon)) },
            )
            GarageListItem(
                leading = { GarageListIcon(Icons.Rounded.Upload) },
                headline = { Text(stringResource(R.string.settings_import)) },
                supporting = { Text(stringResource(R.string.settings_coming_soon)) },
            )
            GarageListItem(
                leading = { GarageListIcon(Icons.Rounded.DeleteSweep, background = colors.dangerMuted) },
                headline = { Text(stringResource(R.string.settings_clear)) },
                supporting = { Text(stringResource(R.string.settings_clear_hint)) },
                tone = GarageTone.Danger,
                onClick = { onEvent(SettingsContract.Intent.Show(SettingsContract.Dialog.CLEAR)) },
            )
        }

        Section(R.string.settings_account)
        GarageListGroup(Modifier.fillMaxWidth()) {
            GarageListItem(
                leading = { GarageListIcon(Icons.AutoMirrored.Rounded.Logout, background = colors.dangerMuted) },
                headline = { Text(stringResource(R.string.settings_sign_out)) },
                tone = GarageTone.Danger,
                onClick = { onEvent(SettingsContract.Intent.Show(SettingsContract.Dialog.SIGN_OUT)) },
            )
        }

        Section(R.string.settings_about)
        GarageListGroup(Modifier.fillMaxWidth()) {
            GarageListItem(
                leading = { GarageListIcon(Icons.Rounded.Garage, tint = colors.primary) },
                headline = { Text(stringResource(R.string.settings_about_title)) },
                supporting = { Text(stringResource(R.string.settings_about_text)) },
            )
        }
    }

    when (sheet) {
        SettingsContract.Sheet.CURRENCY -> GarageBottomSheet(
            onDismissRequest = { onEvent(SettingsContract.Intent.OpenSheet(null)) },
            title = { Text(stringResource(R.string.settings_currency)) },
        ) {
            GarageListGroup(Modifier.fillMaxWidth()) {
                Currencies.forEach { code ->
                    GarageListItem(
                        headline = { Text(code) },
                        trailing = { if (code == currency) Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.primary) },
                        onClick = { onEvent(SettingsContract.Intent.SetCurrency(code)) },
                    )
                }
            }
        }
        SettingsContract.Sheet.INTERVALS -> GarageBottomSheet(
            onDismissRequest = { onEvent(SettingsContract.Intent.OpenSheet(null)) },
            title = { Text(stringResource(R.string.settings_intervals)) },
        ) {
            Text(stringResource(R.string.settings_intervals_sheet_hint), style = GarageDesignTheme.typography.body.copy(color = colors.dim), modifier = Modifier.padding(bottom = spacing.sm))
            GarageListGroup(Modifier.fillMaxWidth()) {
                intervals.forEach { interval ->
                    GarageListItem(
                        headline = { Text(interval.name) },
                        trailing = {
                            GarageIconButton(Icons.Rounded.Remove, null, onClick = {
                                onEvent(SettingsContract.Intent.SetIntervalMonths(interval.id, interval.months - 1))
                            }, enabled = interval.months > 1)
                            Text(
                                pluralStringResource(R.plurals.settings_months, interval.months, interval.months),
                                color = colors.text,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(56.dp),
                            )
                            GarageIconButton(Icons.Rounded.Add, null, onClick = {
                                onEvent(SettingsContract.Intent.SetIntervalMonths(interval.id, interval.months + 1))
                            })
                        },
                    )
                }
            }
        }
        null -> Unit
    }

    dialog?.let { current ->
        val signOut = current == SettingsContract.Dialog.SIGN_OUT
        GarageDialog(
            onDismissRequest = { onEvent(SettingsContract.Intent.Dismiss) },
            icon = if (signOut) Icons.AutoMirrored.Rounded.Logout else Icons.Rounded.DeleteSweep,
            tone = if (signOut) GarageTone.Default else GarageTone.Danger,
            title = { Text(stringResource(if (signOut) R.string.settings_sign_out_title else R.string.settings_clear_title)) },
            text = { Text(stringResource(if (signOut) R.string.settings_sign_out_text else R.string.settings_clear_text)) },
            dismissButton = { GarageDialogButton(stringResource(com.almudier.mygarage.core.presentation.R.string.common_cancel), onClick = { onEvent(SettingsContract.Intent.Dismiss) }) },
            confirmButton = {
                GarageDialogButton(
                    stringResource(if (signOut) R.string.settings_sign_out else R.string.settings_clear_confirm),
                    onClick = { onEvent(SettingsContract.Intent.Confirm) },
                    tone = if (signOut) GarageTone.Default else GarageTone.Danger,
                    emphasized = true,
                    loading = signOut && signingOut,
                )
            },
        )
    }
}

@Composable
private fun Section(title: Int) {
    GarageSectionHeader(stringResource(title), Modifier.padding(top = GarageDesignTheme.spacing.sm, bottom = 2.dp))
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun SettingsScreenPreview() {
    SettingsScreenContent(dialog = null, sheet = null, currency = "JOD", intervals = DefaultServiceIntervals, onEvent = {})
}
