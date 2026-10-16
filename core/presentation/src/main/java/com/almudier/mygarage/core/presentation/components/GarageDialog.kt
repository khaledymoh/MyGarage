package com.almudier.mygarage.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme

/**
 * Confirm dialog for destructive or account actions. Title, text and buttons are slots;
 * use [GarageDialogButton] for the actions.
 */
@Composable
fun GarageDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    text: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null,
    tone: GarageTone = GarageTone.Danger,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        GarageDialogContent(title, confirmButton, modifier, icon, text, dismissButton, tone)
    }
}

@Composable
private fun GarageDialogContent(
    title: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier,
    icon: ImageVector?,
    text: (@Composable () -> Unit)?,
    dismissButton: (@Composable () -> Unit)?,
    tone: GarageTone,
) {
    val colors = GarageDesignTheme.colors
    val shape = RoundedCornerShape(GarageDesignTheme.radius.lg)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(24.dp, shape)
            .clip(shape)
            .background(colors.surface)
            .border(GarageDesignTheme.stroke.thin, colors.line, shape)
            .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 14.dp),
    ) {
        if (icon != null) {
            val danger = tone == GarageTone.Danger
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(GarageDesignTheme.radius.sm))
                    .background(if (danger) colors.dangerMuted else colors.primaryMuted),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = if (danger) colors.danger else colors.primary)
            }
            Box(Modifier.size(12.dp))
        }
        CompositionLocalProvider(LocalTextStyle provides GarageDesignTheme.typography.h2.copy(fontSize = 17.sp)) { title() }
        if (text != null) {
            Box(Modifier.padding(top = 6.dp)) {
                CompositionLocalProvider(LocalTextStyle provides GarageDesignTheme.typography.body.copy(color = colors.dim)) { text() }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            dismissButton?.invoke()
            confirmButton()
        }
    }
}

/**
 * Text action for dialogs and sheets. Emphasized actions get a tinted background. While [loading]
 * it shows a spinner in place of the label (keeping its size) and ignores taps.
 */
@Composable
fun GarageDialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: GarageTone = GarageTone.Default,
    emphasized: Boolean = false,
    loading: Boolean = false,
) {
    val colors = GarageDesignTheme.colors
    val content = when {
        !emphasized -> colors.dim
        tone == GarageTone.Danger -> colors.danger
        else -> colors.primary
    }
    val fill = when {
        !emphasized -> Color.Transparent
        tone == GarageTone.Danger -> colors.dangerMuted
        else -> colors.primaryMuted
    }
    Box(
        modifier
            .clip(RoundedCornerShape(GarageDesignTheme.radius.xs))
            .background(fill)
            .clickable(enabled = !loading, role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides content) {
            Text(
                text,
                style = GarageDesignTheme.typography.label.copy(color = content, fontWeight = FontWeight.Bold),
                modifier = Modifier.alpha(if (loading) 0f else 1f),
            )
            if (loading) CircularProgressIndicator(Modifier.size(16.dp), color = content, strokeWidth = 2.dp)
        }
    }
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun GarageDialogPreview() {
    Box(Modifier.padding(20.dp)) {
        GarageDialogContent(
            title = { Text("Delete this maintenance visit?") },
            text = { Text("This removes the visit and its 2 items (35 JOD). This can't be undone.") },
            confirmButton = { GarageDialogButton("Delete visit", {}, tone = GarageTone.Danger, emphasized = true) },
            dismissButton = { GarageDialogButton("Cancel", {}) },
            modifier = Modifier,
            icon = Icons.Rounded.Delete,
            tone = GarageTone.Danger,
        )
    }
}
