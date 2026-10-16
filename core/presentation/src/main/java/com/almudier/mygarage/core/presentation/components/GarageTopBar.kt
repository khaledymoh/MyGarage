package com.almudier.mygarage.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme

/** Top bar for pushed screens. Navigation, title and actions are slots. */
@Composable
fun GarageTopBar(
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = GarageDesignTheme.colors.background,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        navigationIcon?.invoke()
        Box(Modifier.weight(1f).padding(horizontal = 6.dp)) {
            CompositionLocalProvider(LocalTextStyle provides GarageDesignTheme.typography.h3.copy(fontSize = 16.sp)) {
                title?.invoke()
            }
        }
        actions()
    }
}

/** Two-line title: an optional eyebrow (e.g. the car) above the screen name. */
@Composable
fun GarageTopBarTitle(title: String, eyebrow: String? = null) {
    Column {
        if (eyebrow != null) {
            Text(eyebrow.uppercase(), style = GarageDesignTheme.typography.eyebrow.copy(fontSize = 10.sp), maxLines = 1)
        }
        Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun GarageTopBarPreview() {
    Column {
        GarageTopBar(
            navigationIcon = { GarageIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onClick = {}) },
            title = { GarageTopBarTitle("Add maintenance", eyebrow = "BMW 330e") },
        )
        GarageTopBar(
            navigationIcon = { GarageIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onClick = {}) },
            actions = {
                GarageIconButton(Icons.Rounded.Edit, "Edit", onClick = {})
                GarageIconButton(Icons.Rounded.Delete, "Delete", onClick = {}, style = GarageIconButtonStyle.Danger)
            },
        )
    }
}
