package com.almudier.mygarage.core.presentation.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.almudier.mygarage.core.domain.model.RecordType
import com.almudier.mygarage.core.domain.model.Vehicle
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.R
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.components.GarageCard
import com.almudier.mygarage.core.presentation.components.GarageCardStyle
import com.almudier.mygarage.core.presentation.components.GarageIconButton
import com.almudier.mygarage.core.presentation.components.GarageIconButtonStyle
import com.almudier.mygarage.core.presentation.components.GarageListItem
import com.almudier.mygarage.core.presentation.components.GarageTab
import com.almudier.mygarage.core.presentation.components.GarageTabs
import com.almudier.mygarage.core.presentation.components.rememberPhotoModel
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme

/** Maintenance / Upgrade / Detailing tabs on the add screen. */
@Composable
fun RecordTypeTabs(selected: RecordType, onSelect: (RecordType) -> Unit, modifier: Modifier = Modifier) {
    GarageTabs(modifier) {
        RecordType.entries.forEach { type ->
            GarageTab(
                text = stringResource(type.tabLabel),
                selected = type == selected,
                onClick = { onSelect(type) },
                dot = type.color,
            )
        }
    }
}

private val CarSilhouette = Color(0xFFDBE5F3)

/**
 * The car picture: the user's photo, or a silhouette over a soft glow in the primary color.
 * With [glow] off it renders as a compact tile (for headers).
 */
@Composable
fun CarVisual(vehicle: Vehicle, modifier: Modifier = Modifier, glow: Boolean = true) {
    val colors = GarageDesignTheme.colors
    val photo = vehicle.photoUri
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(GarageDesignTheme.radius.xs))
            .then(if (!glow) Modifier.background(colors.surface) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        val iconSize = minOf(maxWidth * 0.55f, maxHeight * 0.8f)
        if (photo != null) {
            AsyncImage(
                model = rememberPhotoModel(photo),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            val glowColor = colors.primary.copy(alpha = 0.26f)
            Box(
                Modifier
                    .fillMaxSize()
                    .then(
                        if (glow) Modifier.drawBehind {
                            drawOval(
                                brush = Brush.radialGradient(
                                    listOf(glowColor, Color.Transparent),
                                    center = Offset(size.width / 2, size.height * 0.6f),
                                    radius = size.minDimension * 0.9f,
                                ),
                                topLeft = Offset(size.width * 0.05f, size.height * 0.1f),
                                size = androidx.compose.ui.geometry.Size(size.width * 0.9f, size.height * 0.85f),
                            )
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.DirectionsCar,
                    contentDescription = null,
                    tint = CarSilhouette,
                    modifier = Modifier.size(iconSize),
                )
            }
        }
    }
}

/**
 * Attach a receipt or photo. Empty: dashed card. Attached: thumbnail with a remove button.
 * [photo] is a content URI or a `data:` URI, like [Vehicle.photoUri].
 */
@Composable
fun AttachmentField(
    photo: String?,
    onPick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.attachment_receipt),
) {
    val colors = GarageDesignTheme.colors
    if (photo == null) {
        GarageCard(
            modifier = modifier.fillMaxWidth(),
            style = GarageCardStyle.Dashed,
            onClick = onPick,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 18.dp, horizontal = 12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null, tint = colors.dim, modifier = Modifier.size(22.dp))
                Text(label, style = GarageDesignTheme.typography.body, color = colors.dim)
            }
        }
    } else {
        GarageCard(modifier = modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            GarageListItem(
                onClick = onPick,
                leading = {
                    AsyncImage(
                        model = rememberPhotoModel(photo),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(GarageDesignTheme.radius.xs)).background(colors.surface1),
                    )
                },
                headline = { Text(stringResource(R.string.attachment_attached)) },
                trailing = {
                    GarageIconButton(Icons.Rounded.Close, stringResource(R.string.attachment_remove), onClick = onRemove, style = GarageIconButtonStyle.Ghost)
                },
            )
        }
    }
}

/** A receipt or photo image, cropped to fill [modifier]. */
@Composable
fun ReceiptPreview(photo: String, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Crop) {
    AsyncImage(
        model = rememberPhotoModel(photo),
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier.clip(RoundedCornerShape(GarageDesignTheme.radius.sm)).background(GarageDesignTheme.colors.surface1),
    )
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun SharedCompositionsPreview() {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RecordTypeTabs(RecordType.Upgrade, onSelect = {})
        CarVisual(Vehicle("v", "BMW", "330e", 2020, "M Sport", 48600, "", color = "Grey"), Modifier.fillMaxWidth().height(120.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Bottom) {
            Amount(2290.0, size = AmountSize.Hero)
            Amount(755.0, size = AmountSize.Large)
            Amount(35.0)
            Amount(0.0)
        }
        AttachmentField(photo = null, onPick = {}, onRemove = {})
    }
}
