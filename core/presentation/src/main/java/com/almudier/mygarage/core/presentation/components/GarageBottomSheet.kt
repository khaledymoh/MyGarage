package com.almudier.mygarage.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme

/**
 * Bottom sheet for filters and short choices. Show it conditionally (`if (open) GarageBottomSheet(...)`).
 * [title] and [titleAction] (e.g. a "Reset" text action) sit on the header row. With
 * [dismissible] false it can't be swiped away, tapped outside or closed with back, and has no
 * drag handle: use it only for states the user must act on.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    titleAction: (@Composable () -> Unit)? = null,
    dismissible: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = GarageDesignTheme.colors
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { dismissible || it != SheetValue.Hidden },
    )
    ModalBottomSheet(
        onDismissRequest = { if (dismissible) onDismissRequest() },
        sheetState = sheetState,
        sheetGesturesEnabled = dismissible,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = dismissible),
        shape = RoundedCornerShape(topStart = GarageDesignTheme.radius.lg, topEnd = GarageDesignTheme.radius.lg),
        containerColor = colors.surface,
        contentColor = colors.text,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = if (dismissible) {
            {
                Box(
                    Modifier
                        .padding(top = 10.dp, bottom = 12.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(GarageDesignTheme.radius.xxs))
                        .background(colors.line),
                )
            }
        } else {
            null
        },
    ) {
        Column(
            modifier
                .fillMaxWidth()
                // No drag handle on a non-dismissible sheet: give the content that space instead.
                .padding(start = 14.dp, end = 14.dp, top = if (dismissible) 0.dp else 22.dp)
                .navigationBarsPadding()
                .padding(bottom = 18.dp),
        ) {
            if (title != null || titleAction != null) {
                Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) {
                        CompositionLocalProvider(LocalTextStyle provides GarageDesignTheme.typography.h2.copy(fontSize = 16.sp)) {
                            title?.invoke()
                        }
                    }
                    titleAction?.invoke()
                }
            }
            content()
            Spacer(Modifier.height(4.dp))
        }
    }
}
