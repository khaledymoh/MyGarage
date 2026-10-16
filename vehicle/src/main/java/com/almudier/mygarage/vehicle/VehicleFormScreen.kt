package com.almudier.mygarage.vehicle

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.almudier.mygarage.core.domain.model.Vehicle
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.components.GarageButton
import com.almudier.mygarage.core.presentation.components.GarageCard
import com.almudier.mygarage.core.presentation.components.GarageCardStyle
import com.almudier.mygarage.core.presentation.components.GarageChip
import com.almudier.mygarage.core.presentation.components.GarageChipStyle
import com.almudier.mygarage.core.presentation.components.GarageIconButton
import com.almudier.mygarage.core.presentation.components.GarageIconButtonStyle
import com.almudier.mygarage.core.presentation.components.GarageTextField
import com.almudier.mygarage.core.presentation.components.GarageTopBar
import com.almudier.mygarage.core.presentation.components.GarageTopBarTitle
import com.almudier.mygarage.core.presentation.formatNumber
import com.almudier.mygarage.core.presentation.shared.CarVisual
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme
import com.almudier.mygarage.vehicle.api.VehicleFormRoute
import com.almudier.mygarage.vehicle.R as ResR

@Composable
fun VehicleFormScreen(route: VehicleFormRoute, viewModel: VehicleFormViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(route.vehicleId) { viewModel.onEvent(VehicleFormContract.Intent.Load(route.vehicleId)) }
    VehicleFormContent(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun VehicleFormContent(
    state: VehicleFormContract.State,
    onEvent: (VehicleFormContract.Intent) -> Unit,
) {
    val colors = GarageDesignTheme.colors
    val spacing = GarageDesignTheme.spacing
    val keyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    fun update(field: String, value: String) = onEvent(VehicleFormContract.Intent.Update(field, value))
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { update("photoUri", it.toString()) }
    }
    val photo = state.photoUri?.takeIf { it.isNotBlank() }

    Column(Modifier.fillMaxSize().background(colors.background)) {
        GarageTopBar(
            navigationIcon = { GarageIconButton(Icons.Rounded.Close, null, onClick = { onEvent(VehicleFormContract.Intent.Back) }) },
            title = { GarageTopBarTitle(stringResource(if (state.editing) ResR.string.vehicle_form_edit_title else ResR.string.vehicle_form_add_title)) },
        )
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = colors.primary) }
            return@Column
        }
        Column(
            Modifier
                .weight(1f)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.nr)
                .padding(bottom = spacing.lr),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (photo == null) {
                GarageCard(
                    Modifier.fillMaxWidth(),
                    style = GarageCardStyle.Dashed,
                    onClick = { pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentPadding = PaddingValues(vertical = 22.dp),
                ) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Rounded.AddAPhoto, contentDescription = null, tint = colors.dim, modifier = Modifier.size(26.dp))
                        Text(stringResource(ResR.string.vehicle_form_photo_label), style = GarageDesignTheme.typography.body.copy(color = colors.dim))
                        Text(stringResource(ResR.string.vehicle_form_photo_hint), style = GarageDesignTheme.typography.caption.copy(color = colors.mute))
                    }
                }
            } else {
                Box {
                    CarVisual(
                        Vehicle("", state.manufacturer, state.model, 0, "", 0, "", color = state.color, photoUri = photo),
                        Modifier.fillMaxWidth().height(180.dp),
                    )
                    Row(Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                        GarageIconButton(Icons.Rounded.AddAPhoto, stringResource(ResR.string.vehicle_form_photo_label), onClick = {
                            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        })
                        GarageIconButton(Icons.Rounded.Delete, null, onClick = { update("photoUri", "") }, style = GarageIconButtonStyle.Danger)
                    }
                }
            }

            GarageTextField(
                state.manufacturer, { update("manufacturer", it) },
                label = stringResource(ResR.string.vehicle_form_manufacturer_label),
                placeholder = stringResource(ResR.string.vehicle_form_manufacturer_placeholder),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                GarageTextField(
                    state.model, { update("model", it) },
                    label = stringResource(ResR.string.vehicle_form_model_label),
                    placeholder = stringResource(ResR.string.vehicle_form_model_placeholder),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.weight(1.4f),
                )
                GarageTextField(
                    state.year, { update("year", it) },
                    label = stringResource(ResR.string.vehicle_form_year_label),
                    placeholder = stringResource(ResR.string.vehicle_form_year_placeholder),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }
            GarageTextField(
                state.trim, { update("trim", it) },
                label = stringResource(ResR.string.vehicle_form_trim_label),
                placeholder = stringResource(ResR.string.vehicle_form_trim_placeholder),
            )
            GarageTextField(
                state.mileage, { update("mileage", it) },
                label = stringResource(ResR.string.vehicle_form_mileage_label),
                placeholder = stringResource(ResR.string.vehicle_form_mileage_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailing = { Text("km") },
                isError = state.isMileageBelowLatestService,
                supportingText = if (state.isMileageBelowLatestService) {
                    stringResource(ResR.string.vehicle_form_mileage_below_service, formatNumber(state.minMileage))
                } else null,
            )
            if (state.isMileageBelowLatestService) {
                GarageChip(
                    text = stringResource(ResR.string.vehicle_form_use_mileage, formatNumber(state.minMileage)),
                    selected = true,
                    onClick = { update("mileage", state.minMileage.toString()) },
                    style = GarageChipStyle.Tinted(colors.primary),
                    icon = Icons.Rounded.AutoFixHigh,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                GarageTextField(
                    state.plate, { update("plate", it) },
                    label = stringResource(ResR.string.vehicle_form_plate_label),
                    placeholder = stringResource(ResR.string.vehicle_form_plate_placeholder),
                    textStyle = GarageDesignTheme.typography.number.copy(fontSize = GarageDesignTheme.typography.h3.fontSize),
                    modifier = Modifier.weight(1f),
                )
                GarageTextField(
                    state.color, { update("color", it) },
                    label = stringResource(ResR.string.vehicle_form_color_label),
                    placeholder = stringResource(ResR.string.vehicle_form_color_placeholder),
                    modifier = Modifier.weight(1f),
                )
            }
            GarageTextField(
                state.vin, { update("vin", it) },
                label = stringResource(ResR.string.vehicle_form_vin_label),
                placeholder = stringResource(ResR.string.vehicle_form_vin_hint),
                textStyle = GarageDesignTheme.typography.number.copy(fontSize = GarageDesignTheme.typography.h3.fontSize),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            )
        }
        if (!keyboardOpen) {
            GarageButton(
                text = stringResource(if (state.editing) ResR.string.vehicle_form_save_changes else ResR.string.vehicle_form_add_title),
                onClick = { onEvent(VehicleFormContract.Intent.Save) },
                enabled = state.canSave,
                loading = state.isSaving,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = spacing.nr, vertical = spacing.sm),
            )
        }
    }
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun VehicleFormPreview() {
    VehicleFormContent(
        state = VehicleFormContract.State(
            editing = true, manufacturer = "BMW", model = "330e", year = "2020", trim = "M Sport",
            mileage = "47900", minMileage = 48600, plate = "21-88330", color = "Mineral grey",
        ),
        onEvent = {},
    )
}
