package com.almudier.mygarage.vehicle.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.almudier.mygarage.core.domain.model.Vehicle
import com.almudier.mygarage.core.presentation.components.GarageIconButton
import com.almudier.mygarage.core.presentation.shared.CarVisual
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme
import com.almudier.mygarage.vehicle.R

/**
 * Top of Dash, Log and Insights: tap the car for its profile, ⇄ to go back to the garage.
 * There are no car tabs; the whole app follows the car picked in the garage.
 */
@Composable
fun CarHeader(
    vehicle: Vehicle,
    onOpenProfile: () -> Unit,
    onSwitchCar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            Modifier.weight(1f).clickable(onClick = onOpenProfile),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CarVisual(vehicle, Modifier.size(width = 52.dp, height = 36.dp), glow = false)
            Column(Modifier.weight(1f)) {
                Text(
                    "${vehicle.manufacturer} · ${vehicle.year}".uppercase(),
                    style = GarageDesignTheme.typography.eyebrow.copy(fontSize = 10.sp),
                    maxLines = 1,
                )
                Text(
                    listOf(vehicle.model, vehicle.trim).filter { it.isNotBlank() }.joinToString(" "),
                    style = GarageDesignTheme.typography.h3.copy(fontSize = 16.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        GarageIconButton(Icons.Rounded.SwapHoriz, stringResource(R.string.car_switch), onClick = onSwitchCar)
    }
}
