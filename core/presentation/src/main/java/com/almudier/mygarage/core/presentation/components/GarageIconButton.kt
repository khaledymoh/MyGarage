package com.almudier.mygarage.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme

enum class GarageIconButtonStyle { Outlined, Ghost, Danger, Filled }

enum class GarageIconButtonSize { Regular, Large }

/**
 * Square icon-only button. The visible box is 36dp (48dp when [GarageIconButtonSize.Large]);
 * the touch target is always at least 48dp.
 */
@Composable
fun GarageIconButton(
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    style: GarageIconButtonStyle = GarageIconButtonStyle.Outlined,
    size: GarageIconButtonSize = GarageIconButtonSize.Regular,
    enabled: Boolean = true,
    icon: @Composable () -> Unit,
) {
    val colors = GarageDesignTheme.colors
    val shape = RoundedCornerShape(GarageDesignTheme.radius.sm)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val large = size == GarageIconButtonSize.Large
    val boxSize = if (large) 48.dp else 36.dp
    val tint = when (style) {
        GarageIconButtonStyle.Danger -> colors.danger
        GarageIconButtonStyle.Filled -> colors.onPrimary
        else -> colors.text
    }
    val fill = when (style) {
        GarageIconButtonStyle.Filled -> colors.primary
        GarageIconButtonStyle.Ghost -> if (pressed) colors.surface1 else Color.Transparent
        else -> if (pressed) colors.surface2 else colors.surface
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics {
                role = Role.Button
                if (contentDescription != null) this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(boxSize)
                .scale(if (style == GarageIconButtonStyle.Filled && pressed) 0.94f else 1f)
                .then(
                    if (style == GarageIconButtonStyle.Filled) {
                        Modifier.shadow(12.dp, shape, ambientColor = colors.primary, spotColor = colors.primary)
                    } else Modifier
                )
                .clip(shape)
                .background(fill)
                .then(
                    if (style == GarageIconButtonStyle.Outlined || style == GarageIconButtonStyle.Danger) {
                        Modifier.border(GarageDesignTheme.stroke.thin, colors.line, shape)
                    } else Modifier
                )
                .indication(interaction, ripple(color = tint)),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides tint) { icon() }
        }
    }
}

/** Convenience overload for an [ImageVector] icon. */
@Composable
fun GarageIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: GarageIconButtonStyle = GarageIconButtonStyle.Outlined,
    size: GarageIconButtonSize = GarageIconButtonSize.Regular,
    enabled: Boolean = true,
) {
    GarageIconButton(
        onClick = onClick,
        contentDescription = contentDescription,
        modifier = modifier,
        style = style,
        size = size,
        enabled = enabled,
    ) {
        Icon(icon, contentDescription = null, Modifier.size(if (size == GarageIconButtonSize.Large) 26.dp else 20.dp))
    }
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun GarageIconButtonPreview() {
    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        GarageIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onClick = {})
        GarageIconButton(Icons.Rounded.Edit, "Edit", onClick = {})
        GarageIconButton(Icons.Rounded.Delete, "Delete", onClick = {}, style = GarageIconButtonStyle.Danger)
        GarageIconButton(Icons.Rounded.Close, "Close", onClick = {}, style = GarageIconButtonStyle.Ghost)
        GarageIconButton(Icons.Rounded.Add, "Add", onClick = {}, style = GarageIconButtonStyle.Filled, size = GarageIconButtonSize.Large)
    }
}
