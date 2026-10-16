package com.almudier.mygarage.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.extension.clearFocusOnKeyboardDismiss
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme

enum class GarageTextFieldStyle { Boxed, Search }

/**
 * Generic text input with the label inside the box. Leading and trailing are slots,
 * so units ("km", "JOD"), icons and clear buttons are the caller's choice.
 * Pass [onClick] together with `readOnly = true` for picker fields (dates, selects).
 */
@Composable
fun GarageTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: GarageTextFieldStyle = GarageTextFieldStyle.Boxed,
    label: String? = null,
    placeholder: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    textStyle: TextStyle = GarageDesignTheme.typography.h3.copy(fontSize = 14.sp),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onClick: (() -> Unit)? = null,
) {
    val colors = GarageDesignTheme.colors
    val shape = RoundedCornerShape(GarageDesignTheme.radius.sm)
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val search = style == GarageTextFieldStyle.Search
    val borderColor = when {
        isError -> colors.danger
        focused || (search && value.isNotEmpty()) -> colors.primary
        else -> colors.line
    }

    Column(modifier.alpha(if (enabled) 1f else 0.45f)) {
        Box {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth().clearFocusOnKeyboardDismiss(),
                enabled = enabled && onClick == null,
                readOnly = readOnly,
                singleLine = singleLine,
                maxLines = if (singleLine) 1 else 6,
                textStyle = textStyle.copy(color = colors.text),
                keyboardOptions = keyboardOptions,
                interactionSource = interaction,
                cursorBrush = SolidColor(colors.primary),
                decorationBox = { inner ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (search) Modifier.height(44.dp) else Modifier.defaultMinSize(minHeight = 52.dp))
                            .clip(shape)
                            .background(colors.surface)
                            .border(if (focused && !search) 1.5.dp else GarageDesignTheme.stroke.thin, borderColor, shape)
                            .padding(horizontal = GarageDesignTheme.spacing.sm, vertical = if (search) 0.dp else 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GarageDesignTheme.spacing.xs),
                    ) {
                        CompositionLocalProvider(LocalContentColor provides if (search) colors.dim else colors.mute) {
                            leading?.invoke()
                        }
                        Column(Modifier.weight(1f)) {
                            if (label != null && !search) {
                                Text(
                                    label.uppercase(),
                                    style = GarageDesignTheme.typography.eyebrow.copy(fontSize = 10.sp, letterSpacing = 1.sp),
                                )
                            }
                            Box {
                                if (value.isEmpty() && placeholder != null) {
                                    Text(placeholder, style = textStyle.copy(color = colors.mute, fontWeight = FontWeight.Medium))
                                }
                                inner()
                            }
                        }
                        val trailingStyle = GarageDesignTheme.typography.label.copy(color = colors.dim, fontSize = 12.sp)
                        CompositionLocalProvider(LocalContentColor provides colors.dim, LocalTextStyle provides trailingStyle) {
                            trailing?.invoke()
                        }
                    }
                },
            )
            if (onClick != null) {
                Box(Modifier.matchParentSize().clip(shape).clickable(enabled = enabled, onClick = onClick))
            }
        }
        if (supportingText != null) {
            Text(
                supportingText,
                style = GarageDesignTheme.typography.caption,
                color = if (isError) colors.danger else colors.mute,
                modifier = Modifier.padding(start = GarageDesignTheme.spacing.xxs, top = GarageDesignTheme.spacing.xxs),
            )
        }
    }
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun GarageTextFieldPreview() {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        GarageTextField("", {}, label = "Workshop", placeholder = "Optional")
        GarageTextField("Bavaria Motors", {}, label = "Workshop")
        GarageTextField(
            "48,600", {}, label = "Mileage",
            textStyle = GarageDesignTheme.typography.number.copy(fontSize = 14.sp),
            trailing = { Text("km") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        GarageTextField(
            "47,900", {}, label = "Current mileage (km) *", isError = true,
            supportingText = "Must be at least 48,600 km, the mileage of the latest service",
        )
        GarageTextField(
            "12 Oct 2026", {}, label = "Date *", readOnly = true, onClick = {},
            trailing = { Icon(Icons.Rounded.CalendarMonth, null, Modifier.size(18.dp)) },
        )
        GarageTextField(
            "", {}, style = GarageTextFieldStyle.Search, placeholder = "Search this car's records…",
            leading = { Icon(Icons.Rounded.Search, null, Modifier.size(18.dp)) },
        )
    }
}
