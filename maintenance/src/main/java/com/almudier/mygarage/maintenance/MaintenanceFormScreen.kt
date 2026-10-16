package com.almudier.mygarage.maintenance

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.almudier.mygarage.core.presentation.components.GarageGlassScaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.almudier.mygarage.core.domain.model.RecordType
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.components.GarageButton
import com.almudier.mygarage.core.presentation.components.GarageCard
import com.almudier.mygarage.core.presentation.components.GarageCardStyle
import com.almudier.mygarage.core.presentation.components.GarageChip
import com.almudier.mygarage.core.presentation.components.GarageChipStyle
import com.almudier.mygarage.core.presentation.components.GarageDialogButton
import com.almudier.mygarage.core.presentation.components.GarageIconButton
import com.almudier.mygarage.core.presentation.components.GarageIconButtonStyle
import com.almudier.mygarage.core.presentation.components.GarageSearchPickerField
import com.almudier.mygarage.core.presentation.components.GarageSectionHeader
import com.almudier.mygarage.core.presentation.components.GarageTextField
import com.almudier.mygarage.core.presentation.components.GarageTone
import com.almudier.mygarage.core.presentation.components.GarageTopBar
import com.almudier.mygarage.core.presentation.components.GarageTopBarTitle
import com.almudier.mygarage.core.presentation.formatDate
import com.almudier.mygarage.core.presentation.pick
import com.almudier.mygarage.core.presentation.shared.Amount
import com.almudier.mygarage.core.presentation.shared.AmountSize
import com.almudier.mygarage.core.presentation.shared.AttachmentField
import com.almudier.mygarage.core.presentation.shared.LocalCurrency
import com.almudier.mygarage.core.presentation.shared.RecordTypeTabs
import com.almudier.mygarage.core.presentation.shared.color
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme
import com.almudier.mygarage.maintenance.api.MaintenanceFormRoute
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import com.almudier.mygarage.core.presentation.R as CoreR
import com.almudier.mygarage.maintenance.R as ResR

@Composable
fun MaintenanceFormScreen(route: MaintenanceFormRoute, viewModel: MaintenanceFormViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(route.vehicleId, route.recordId) {
        viewModel.onEvent(MaintenanceFormContract.Intent.Load(route.vehicleId, route.recordId, RecordType.fromName(route.type)))
    }
    MaintenanceFormContent(
        isEditing = state.recordId != null,
        type = state.type,
        date = state.date,
        mileage = state.mileage,
        workshop = state.workshop,
        notes = state.notes,
        receipt = state.receipt,
        items = state.items,
        services = state.services,
        totalCost = state.totalCost,
        canSave = state.canSave,
        isSaving = state.isSaving,
        isLoading = state.isLoading,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceFormContent(
    isEditing: Boolean,
    type: RecordType,
    date: String,
    mileage: String,
    workshop: String,
    notes: String,
    receipt: String?,
    items: List<MaintenanceFormContract.ItemForm>,
    services: List<String>,
    totalCost: Double,
    canSave: Boolean,
    isSaving: Boolean,
    isLoading: Boolean,
    onEvent: (MaintenanceFormContract.Intent) -> Unit,
) {
    val colors = GarageDesignTheme.colors
    val spacing = GarageDesignTheme.spacing
    val currency = LocalCurrency.current
    var showDatePicker by remember { mutableStateOf(false) }
    val keyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val pickReceipt = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onEvent(MaintenanceFormContract.Intent.SetReceipt(uri.toString()))
    }
    fun update(field: String, value: String) = onEvent(MaintenanceFormContract.Intent.Update(field, value))

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = date.toEpochMillisOrNull())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                GarageDialogButton(stringResource(ResR.string.maintenance_form_date_confirm), emphasized = true, onClick = {
                    pickerState.selectedDateMillis?.let {
                        update("date", Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString())
                    }
                    showDatePicker = false
                })
            },
            dismissButton = { GarageDialogButton(stringResource(CoreR.string.common_cancel), onClick = { showDatePicker = false }) },
            colors = DatePickerDefaults.colors(containerColor = colors.surface),
        ) { DatePicker(state = pickerState) }
    }

    GarageGlassScaffold(
        topBar = {
        GarageTopBar(
            containerColor = Color.Transparent,
            navigationIcon = {
                GarageIconButton(Icons.Rounded.Close, stringResource(ResR.string.form_close), onClick = { onEvent(MaintenanceFormContract.Intent.Back) })
            },
            title = {
                GarageTopBarTitle(
                    stringResource(
                        if (isEditing) type.pick(ResR.string.maintenance_form_edit_title, ResR.string.maintenance_form_edit_upgrade_title, ResR.string.maintenance_form_edit_detailing_title)
                        else type.pick(ResR.string.maintenance_form_add_title, ResR.string.maintenance_form_add_upgrade_title, ResR.string.maintenance_form_add_detailing_title)
                    ),
                )
            },
        )
        },
        bottomBar = if (!keyboardOpen && !isLoading) {
            {
            SaveBar(
                totalCost = totalCost,
                itemCount = items.size,
                saveText = if (isEditing) stringResource(CoreR.string.common_save)
                else stringResource(type.pick(ResR.string.form_save_visit, ResR.string.form_save_upgrade, ResR.string.form_save_detailing)),
                canSave = canSave,
                isSaving = isSaving,
                onSave = { onEvent(MaintenanceFormContract.Intent.Save) },
            )
            }
        } else null,
    ) { glassPadding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = colors.primary) }
            return@GarageGlassScaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(top = glassPadding.calculateTopPadding() + spacing.xs, bottom = glassPadding.calculateBottomPadding())
                .padding(horizontal = spacing.nr)
                .padding(bottom = spacing.lr),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RecordTypeTabs(type, onSelect = { onEvent(MaintenanceFormContract.Intent.SetType(it)) })

            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                GarageTextField(
                    value = runCatching { formatDate(date) }.getOrDefault(date),
                    onValueChange = {},
                    label = stringResource(ResR.string.form_date),
                    readOnly = true,
                    onClick = { showDatePicker = true },
                    trailing = { Icon(Icons.Rounded.CalendarMonth, contentDescription = null, Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f),
                )
                if (type == RecordType.Maintenance) {
                    GarageTextField(
                        value = mileage,
                        onValueChange = { update("mileage", it) },
                        label = stringResource(ResR.string.form_mileage),
                        placeholder = stringResource(ResR.string.form_notes_hint),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = GarageDesignTheme.typography.number.copy(fontSize = GarageDesignTheme.typography.h3.fontSize),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            GarageTextField(
                value = workshop,
                onValueChange = { update("workshop", it) },
                label = stringResource(type.pick(ResR.string.form_workshop, ResR.string.form_installed_by, ResR.string.form_detailer)),
                placeholder = stringResource(ResR.string.form_notes_hint),
            )
            GarageTextField(
                value = notes,
                onValueChange = { update("notes", it) },
                label = stringResource(type.pick(ResR.string.form_visit_notes, ResR.string.form_upgrade_notes, ResR.string.form_detailing_notes)),
                placeholder = stringResource(ResR.string.form_notes_hint),
                singleLine = false,
            )
            AttachmentField(
                photo = receipt,
                onPick = { pickReceipt.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                onRemove = { onEvent(MaintenanceFormContract.Intent.SetReceipt(null)) },
                label = stringResource(ResR.string.form_receipt),
            )

            if (type == RecordType.Detailing) {
                GarageSectionHeader(stringResource(ResR.string.form_what_was_done), Modifier.padding(top = spacing.xs))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        ResR.string.preset_car_wash, ResR.string.preset_interior, ResR.string.preset_full,
                        ResR.string.preset_polish, ResR.string.preset_wax, ResR.string.preset_engine_bay,
                    ).forEach { res ->
                        val preset = stringResource(res)
                        GarageChip(
                            text = preset,
                            selected = items.firstOrNull()?.service.equals(preset, ignoreCase = true),
                            onClick = { onEvent(MaintenanceFormContract.Intent.Preset(preset)) },
                            style = GarageChipStyle.Tinted(RecordType.Detailing.color),
                        )
                    }
                }
            }

            GarageSectionHeader(
                stringResource(if (type == RecordType.Upgrade) ResR.string.form_upgrade_items else ResR.string.form_items, items.size),
                Modifier.padding(top = spacing.xs),
            )
            items.forEachIndexed { index, item ->
                ItemEditor(
                    index = index,
                    item = item,
                    type = type,
                    services = services,
                    currency = currency,
                    canRemove = items.size > 1,
                    onEvent = onEvent,
                )
            }
            GarageCard(
                Modifier.fillMaxWidth(),
                style = GarageCardStyle.Dashed,
                onClick = { onEvent(MaintenanceFormContract.Intent.AddItem) },
            ) {
                Text(
                    stringResource(if (type == RecordType.Upgrade) ResR.string.form_add_upgrade else ResR.string.form_add_item),
                    style = GarageDesignTheme.typography.label.copy(color = colors.dim),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

/** Sticky bottom bar: the running total (with item count) beside the Save button. */
@Composable
private fun SaveBar(
    totalCost: Double,
    itemCount: Int,
    saveText: String,
    canSave: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit,
) {
    val colors = GarageDesignTheme.colors
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = GarageDesignTheme.spacing.nr, end = GarageDesignTheme.spacing.nr, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GarageDesignTheme.spacing.sm),
        ) {
            Column(Modifier.weight(1f)) {
                GarageSectionHeader(
                    "${stringResource(ResR.string.form_total)} · ${pluralStringResource(ResR.plurals.detail_items_count, itemCount, itemCount)}",
                )
                Amount(totalCost, size = AmountSize.Large, modifier = Modifier.padding(top = 2.dp))
            }
            GarageButton(
                text = saveText,
                onClick = onSave,
                enabled = canSave,
                loading = isSaving,
                modifier = Modifier.weight(1.15f),
            )
        }
    }
}

/** One item: service, parts and labor (no labor for detailing), item notes. */
@Composable
private fun ItemEditor(
    index: Int,
    item: MaintenanceFormContract.ItemForm,
    type: RecordType,
    services: List<String>,
    currency: String,
    canRemove: Boolean,
    onEvent: (MaintenanceFormContract.Intent) -> Unit,
) {
    fun update(field: String, value: String) = onEvent(MaintenanceFormContract.Intent.UpdateItem(index, field, value))
    val numberStyle = GarageDesignTheme.typography.number.copy(fontSize = GarageDesignTheme.typography.h3.fontSize)
    GarageCard(Modifier.fillMaxWidth(), style = GarageCardStyle.Raised, contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GarageSectionHeader(stringResource(ResR.string.form_item, index + 1), Modifier.weight(1f))
            if (canRemove) {
                GarageIconButton(
                    Icons.Rounded.Delete, stringResource(ResR.string.maintenance_form_delete_item),
                    onClick = { onEvent(MaintenanceFormContract.Intent.RemoveItem(index)) },
                    style = GarageIconButtonStyle.Ghost,
                )
            } else {
                Spacer(Modifier.height(48.dp))
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            GarageSearchPickerField(
                value = item.service,
                onValueChange = { update("service", it) },
                label = stringResource(type.pick(ResR.string.form_service, ResR.string.form_upgrade, ResR.string.form_detailing_service)),
                options = services,
            )
            if (type == RecordType.Detailing) {
                GarageTextField(
                    value = item.cost, onValueChange = { update("cost", it) },
                    label = stringResource(ResR.string.form_cost, currency), placeholder = "0",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), textStyle = numberStyle,
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GarageTextField(
                        value = item.cost, onValueChange = { update("cost", it) },
                        label = stringResource(ResR.string.form_parts, currency), placeholder = "0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), textStyle = numberStyle,
                        modifier = Modifier.weight(1f),
                    )
                    GarageTextField(
                        value = item.laborCost, onValueChange = { update("labor", it) },
                        label = stringResource(ResR.string.form_labor, currency), placeholder = "0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), textStyle = numberStyle,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            GarageTextField(
                value = item.notes, onValueChange = { update("notes", it) },
                label = stringResource(ResR.string.form_item_notes), placeholder = stringResource(ResR.string.form_notes_hint),
                singleLine = false,
            )
        }
    }
}

private fun String.toEpochMillisOrNull(): Long? =
    runCatching { LocalDate.parse(this).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }.getOrNull()

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun MaintenanceFormPreview() {
    MaintenanceFormContent(
        isEditing = false,
        type = RecordType.Maintenance,
        date = "2026-10-12",
        mileage = "48600",
        workshop = "Bavaria Motors",
        notes = "",
        receipt = null,
        items = listOf(MaintenanceFormContract.ItemForm("Brake fluid flush", "20", "15", "DOT 4, full flush")),
        services = emptyList(),
        totalCost = 35.0,
        canSave = true,
        isSaving = false,
        isLoading = false,
        onEvent = {},
    )
}
