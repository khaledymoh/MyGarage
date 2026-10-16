package com.almudier.mygarage.core.presentation.components

import androidx.compose.ui.unit.dp

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme
import kotlinx.coroutines.flow.collectLatest

enum class GarageSnackbarType { Error, Success, Info }

private data class GarageSnackbarVisuals(
    override val message: String,
    val type: GarageSnackbarType,
) : SnackbarVisuals {
    override val actionLabel: String? = null
    override val withDismissAction: Boolean = false
    override val duration: SnackbarDuration = SnackbarDuration.Short
}

@Composable
fun GarageSnackbarHost(
    manager: SnackbarManager,
    modifier: Modifier = Modifier,
) {
    val hostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(manager) {
        manager.events.collectLatest { event ->
            hostState.showSnackbar(GarageSnackbarVisuals(context.getString(event.messageRes), event.type))
        }
    }

    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        GarageSnackbar(data)
    }
}

@Composable
private fun GarageSnackbar(data: SnackbarData) {
    val type = (data.visuals as? GarageSnackbarVisuals)?.type ?: GarageSnackbarType.Info
    val colors = GarageDesignTheme.colors
    // Inverted so it stands out on the dark UI; errors use danger.
    val container = if (type == GarageSnackbarType.Error) colors.danger else colors.text
    val content = if (type == GarageSnackbarType.Error) Color(0xFF1B0A0C) else colors.background
    val accent = when (type) {
        GarageSnackbarType.Error -> content
        GarageSnackbarType.Success -> Color(0xFF0F8A78)
        GarageSnackbarType.Info -> Color(0xFF1D6FD1)
    }
    val shape = RoundedCornerShape(GarageDesignTheme.radius.sm)

    Snackbar(
        modifier = Modifier.padding(GarageDesignTheme.spacing.nr),
        shape = shape,
        containerColor = container,
        contentColor = content,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .padding(end = GarageDesignTheme.spacing.sm)
                    .size(8.dp)
                    .background(accent, CircleShape)
            )
            Text(
                text = data.visuals.message,
                style = GarageDesignTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
                color = content,
            )
        }
    }
}
