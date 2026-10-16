package com.almudier.mygarage.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme

enum class GarageButtonStyle { Primary, Secondary, Danger, Google }

enum class GarageButtonSize { Regular, Small }

/**
 * Generic action button. The label is a slot, so callers decide what goes inside.
 * Use [GarageButtonStyle.Primary] once per screen for the main action.
 */
@Composable
fun GarageButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: GarageButtonStyle = GarageButtonStyle.Primary,
    size: GarageButtonSize = GarageButtonSize.Regular,
    enabled: Boolean = true,
    loading: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = GarageDesignTheme.colors
    val stroke = GarageDesignTheme.stroke
    val shape = RoundedCornerShape(GarageDesignTheme.radius.sm)
    val container = when (style) {
        GarageButtonStyle.Primary -> colors.primary
        GarageButtonStyle.Secondary, GarageButtonStyle.Danger -> Color.Transparent
        GarageButtonStyle.Google -> Color.White
    }
    val contentColor = when (style) {
        GarageButtonStyle.Primary -> colors.onPrimary
        GarageButtonStyle.Secondary -> colors.text
        GarageButtonStyle.Danger -> colors.danger
        GarageButtonStyle.Google -> Color(0xFF1B1E1C)
    }
    val borderColor = when (style) {
        GarageButtonStyle.Secondary -> colors.line
        GarageButtonStyle.Danger -> colors.danger.copy(alpha = 0.35f)
        else -> null
    }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val background = when {
        !pressed -> container
        container == Color.Transparent -> colors.surface1
        else -> lerp(container, Color.Black, 0.12f)
    }
    val small = size == GarageButtonSize.Small

    Row(
        modifier = modifier
            .height(if (small) 36.dp else 48.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(shape)
            .background(background)
            .then(if (borderColor != null) Modifier.border(stroke.thin, borderColor, shape) else Modifier)
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = contentColor),
                enabled = enabled && !loading,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = if (small) GarageDesignTheme.spacing.sm else GarageDesignTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(GarageDesignTheme.spacing.xs, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val labelStyle = GarageDesignTheme.typography.label.copy(
            color = contentColor,
            fontSize = if (small) 13.sp else 14.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        CompositionLocalProvider(LocalContentColor provides contentColor, LocalTextStyle provides labelStyle) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(18.dp), color = contentColor, strokeWidth = 2.dp)
            } else {
                leading?.invoke()
                content()
            }
        }
    }
}

/** Convenience overload for a text label with an optional leading icon. */
@Composable
fun GarageButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: GarageButtonStyle = GarageButtonStyle.Primary,
    size: GarageButtonSize = GarageButtonSize.Regular,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    GarageButton(
        onClick = onClick,
        modifier = modifier,
        style = style,
        size = size,
        enabled = enabled,
        loading = loading,
        leading = leadingIcon?.let { icon -> { Icon(icon, contentDescription = null, Modifier.size(19.dp)) } },
    ) {
        Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun GarageButtonPreview() {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        GarageButton("Save maintenance visit", onClick = {})
        GarageButton("Add vehicle", onClick = {}, leadingIcon = Icons.Rounded.Add)
        GarageButton("Car profile", onClick = {}, style = GarageButtonStyle.Secondary)
        GarageButton("Delete vehicle", onClick = {}, style = GarageButtonStyle.Danger)
        GarageButton("Save changes", onClick = {}, enabled = false)
        GarageButton("Save", onClick = {}, loading = true)
        GarageButton("Log it", onClick = {}, size = GarageButtonSize.Small)
    }
}
