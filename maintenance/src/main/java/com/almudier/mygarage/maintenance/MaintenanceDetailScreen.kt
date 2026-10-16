package com.almudier.mygarage.maintenance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.almudier.mygarage.core.presentation.components.GarageGlassScaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.almudier.mygarage.core.domain.model.MaintenanceItem
import com.almudier.mygarage.core.domain.model.MaintenanceRecord
import com.almudier.mygarage.core.domain.model.RecordType
import com.almudier.mygarage.core.domain.model.cost
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.components.GarageCard
import com.almudier.mygarage.core.presentation.components.GarageCardStyle
import com.almudier.mygarage.core.presentation.components.GarageDialog
import com.almudier.mygarage.core.presentation.components.GarageDialogButton
import com.almudier.mygarage.core.presentation.components.GarageIconButton
import com.almudier.mygarage.core.presentation.components.GarageIconButtonStyle
import com.almudier.mygarage.core.presentation.components.GarageListGroup
import com.almudier.mygarage.core.presentation.components.GarageListItem
import com.almudier.mygarage.core.presentation.components.GarageSectionHeader
import com.almudier.mygarage.core.presentation.components.GarageTone
import com.almudier.mygarage.core.presentation.components.GarageTopBar
import com.almudier.mygarage.core.presentation.formatDate
import com.almudier.mygarage.core.presentation.formatNumber
import com.almudier.mygarage.core.presentation.pick
import com.almudier.mygarage.core.presentation.shared.Amount
import com.almudier.mygarage.core.presentation.shared.AmountSize
import com.almudier.mygarage.core.presentation.shared.LocalCurrency
import com.almudier.mygarage.core.presentation.shared.ReceiptPreview
import com.almudier.mygarage.core.presentation.shared.color
import com.almudier.mygarage.core.presentation.shared.formatMoney
import com.almudier.mygarage.core.presentation.shared.icon
import com.almudier.mygarage.core.presentation.shared.label
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme
import com.almudier.mygarage.maintenance.api.MaintenanceDetailRoute
import com.almudier.mygarage.maintenance.R as ResR

@Composable
fun MaintenanceDetailScreen(route: MaintenanceDetailRoute, viewModel: MaintenanceDetailViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(route.vehicleId, route.recordId) { viewModel.onEvent(MaintenanceDetailContract.Intent.Load(route.vehicleId, route.recordId)) }
    MaintenanceDetailContent(
        record = state.record,
        receipt = state.receipt,
        showDelete = state.showDelete,
        showReceipt = state.showReceipt,
        isDeleting = state.isDeleting,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun MaintenanceDetailContent(
    record: MaintenanceRecord,
    receipt: String?,
    showDelete: Boolean,
    showReceipt: Boolean,
    isDeleting: Boolean,
    onEvent: (MaintenanceDetailContract.Intent) -> Unit,
) {
    val colors = GarageDesignTheme.colors
    val spacing = GarageDesignTheme.spacing
    val currency = LocalCurrency.current
    val type = record.type

    GarageGlassScaffold(
        topBar = {
        GarageTopBar(
            containerColor = Color.Transparent,
            navigationIcon = {
                GarageIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(ResR.string.detail_back), onClick = { onEvent(MaintenanceDetailContract.Intent.Back) })
            },
            actions = {
                GarageIconButton(Icons.Rounded.Edit, stringResource(ResR.string.detail_edit), onClick = { onEvent(MaintenanceDetailContract.Intent.Edit) })
                GarageIconButton(
                    Icons.Rounded.Delete, stringResource(ResR.string.detail_delete),
                    onClick = { onEvent(MaintenanceDetailContract.Intent.AskDelete) },
                    style = GarageIconButtonStyle.Danger,
                    enabled = !isDeleting,
                )
            },
        )
        },
    ) { glassPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = glassPadding.calculateTopPadding() + spacing.xs)
                .padding(horizontal = spacing.nr)
                .navigationBarsPadding()
                .padding(bottom = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(46.dp).clip(RoundedCornerShape(GarageDesignTheme.radius.sm)).background(type.color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) { Icon(type.icon, contentDescription = null, tint = type.color) }
                Column {
                    Text(stringResource(type.label).uppercase(), style = GarageDesignTheme.typography.eyebrow.copy(color = type.color, fontSize = 10.sp))
                    Text(formatDate(record.date), style = GarageDesignTheme.typography.h1.copy(fontSize = 20.sp))
                }
            }
            Amount(record.cost, size = AmountSize.Hero)

            GarageListGroup(Modifier.fillMaxWidth()) {
                if (type == RecordType.Maintenance) {
                    Fact(
                        stringResource(ResR.string.maintenance_detail_mileage_label),
                        if (record.mileage > 0) "${formatNumber(record.mileage)} km" else stringResource(ResR.string.maintenance_detail_not_recorded),
                    )
                }
                Fact(
                    stringResource(type.pick(ResR.string.maintenance_detail_workshop_label, ResR.string.maintenance_detail_upgrade_workshop_label, ResR.string.maintenance_detail_detailing_workshop_label)),
                    record.workshop.ifBlank { stringResource(ResR.string.maintenance_detail_not_recorded) },
                )
                Fact(stringResource(ResR.string.detail_items), pluralStringResource(ResR.plurals.detail_items_count, record.items.size, record.items.size))
            }

            if (record.notes.isNotBlank()) {
                GarageSectionHeader(stringResource(ResR.string.detail_notes), Modifier.padding(top = spacing.xs))
                GarageCard(Modifier.fillMaxWidth()) {
                    Text(record.notes, style = GarageDesignTheme.typography.body.copy(color = colors.text))
                }
            }

            if (record.hasReceipt) {
                GarageSectionHeader(stringResource(ResR.string.detail_receipt), Modifier.padding(top = spacing.xs))
                if (receipt != null) {
                    ReceiptPreview(
                        receipt,
                        Modifier.fillMaxWidth().height(180.dp).clickable { onEvent(MaintenanceDetailContract.Intent.ShowReceipt(true)) },
                    )
                } else {
                    Box(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(GarageDesignTheme.radius.sm)).background(colors.surface1))
                }
            }

            GarageSectionHeader(stringResource(ResR.string.detail_items), Modifier.padding(top = spacing.xs))
            GarageListGroup(Modifier.fillMaxWidth()) {
                record.items.forEach { item -> ItemRow(item, type) }
            }
        }
    }

    if (showDelete) {
        val items = pluralStringResource(ResR.plurals.detail_items_count, record.items.size, record.items.size)
        GarageDialog(
            onDismissRequest = { onEvent(MaintenanceDetailContract.Intent.CancelDelete) },
            icon = Icons.Rounded.Delete,
            title = { Text(stringResource(type.pick(ResR.string.maintenance_detail_delete_dialog_title, ResR.string.maintenance_detail_upgrade_delete_dialog_title, ResR.string.maintenance_detail_detailing_delete_dialog_title))) },
            text = {
                Text(stringResource(type.pick(ResR.string.detail_delete_text_maintenance, ResR.string.detail_delete_text_upgrade, ResR.string.detail_delete_text_detailing), items, formatMoney(record.cost, currency)))
            },
            dismissButton = { GarageDialogButton(stringResource(com.almudier.mygarage.core.presentation.R.string.common_cancel), onClick = { onEvent(MaintenanceDetailContract.Intent.CancelDelete) }) },
            confirmButton = {
                GarageDialogButton(
                    stringResource(type.pick(ResR.string.maintenance_detail_delete_confirm_button, ResR.string.maintenance_detail_upgrade_delete_confirm_button, ResR.string.maintenance_detail_detailing_delete_confirm_button)),
                    onClick = { onEvent(MaintenanceDetailContract.Intent.ConfirmDelete) },
                    tone = GarageTone.Danger,
                    emphasized = true,
                )
            },
        )
    }

    if (showReceipt && receipt != null) {
        Dialog(onDismissRequest = { onEvent(MaintenanceDetailContract.Intent.ShowReceipt(false)) }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(Modifier.fillMaxSize().clickable { onEvent(MaintenanceDetailContract.Intent.ShowReceipt(false)) }, contentAlignment = Alignment.Center) {
                ReceiptPreview(receipt, Modifier.fillMaxWidth().padding(spacing.nr), contentScale = ContentScale.Fit)
            }
        }
    }
}

@Composable
private fun Fact(label: String, value: String) {
    GarageListItem(
        dense = true,
        headline = { Text(label, color = GarageDesignTheme.colors.dim, style = GarageDesignTheme.typography.body) },
        trailing = { Text(value, color = GarageDesignTheme.colors.text) },
    )
}

/** Item name and amount, the parts/labor split, and the item's own note. */
/** Item name and total, a labelled parts / labor breakdown, and the item's own note. */
@Composable
private fun ItemRow(item: MaintenanceItem, type: RecordType) {
    val colors = GarageDesignTheme.colors
    Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.service, style = GarageDesignTheme.typography.h3, modifier = Modifier.weight(1f).padding(end = 12.dp))
            Amount(item.cost + item.laborCost, size = AmountSize.Medium)
        }
        if (type != RecordType.Detailing) {
            Row(
                Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clip(RoundedCornerShape(GarageDesignTheme.radius.xs))
                    .background(colors.surface1),
            ) {
                CostCell(stringResource(ResR.string.detail_parts), item.cost, Modifier.weight(1f))
                Box(Modifier.fillMaxHeight().width(GarageDesignTheme.stroke.thin).background(colors.line))
                CostCell(stringResource(ResR.string.detail_labor), item.laborCost, Modifier.weight(1f))
            }
        }
        if (item.notes.isNotBlank()) {
            Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.AutoMirrored.Rounded.Notes,
                    contentDescription = null,
                    tint = colors.dim,
                    modifier = Modifier.padding(top = 1.dp).size(16.dp),
                )
                Text(
                    item.notes,
                    style = GarageDesignTheme.typography.body.copy(color = colors.text),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

/** One labelled amount in the parts / labor breakdown. */
@Composable
private fun CostCell(label: String, amount: Double, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text(label.uppercase(), style = GarageDesignTheme.typography.eyebrow.copy(fontSize = 10.sp))
        Amount(amount, size = AmountSize.Medium, modifier = Modifier.padding(top = 2.dp))
    }
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun MaintenanceDetailPreview() {
    MaintenanceDetailContent(
        record = MaintenanceRecord(
            "1", "v", "2026-10-12", 48600, "Bavaria Motors", 35.0, notes = "Booked online, done in 40 minutes.",
            items = listOf(
                MaintenanceItem("Brake fluid flush", 18.0, 12.0, "DOT 4, full flush. Next one due in 2 years."),
                MaintenanceItem("Brake inspection", 0.0, 5.0, "Pads at 60%, discs fine."),
            ),
        ),
        receipt = null,
        showDelete = false,
        showReceipt = false,
        isDeleting = false,
        onEvent = {},
    )
}
