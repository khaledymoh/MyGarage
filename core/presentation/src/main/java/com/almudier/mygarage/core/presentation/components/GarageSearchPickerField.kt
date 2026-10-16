package com.almudier.mygarage.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.almudier.mygarage.core.presentation.R
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme

/**
 * A read-only field that opens a searchable sheet of [options]. Matches are highlighted, the
 * current value is checked, and typing something new offers "Add …" as the first row.
 */
@Composable
fun GarageSearchPickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    searchHint: String = stringResource(R.string.picker_search_hint),
    placeholder: String? = null,
) {
    var showSheet by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    GarageTextField(
        value = value,
        onValueChange = {},
        label = label,
        placeholder = placeholder ?: stringResource(R.string.picker_choose),
        readOnly = true,
        onClick = {
            focusManager.clearFocus()
            showSheet = true
        },
        trailing = { Icon(Icons.Rounded.ExpandMore, contentDescription = null, Modifier.size(20.dp)) },
        modifier = modifier,
    )

    if (showSheet) {
        PickerSheet(
            title = label.trimEnd('*', ' '),
            value = value,
            options = options,
            searchHint = searchHint,
            onPick = {
                onValueChange(it)
                showSheet = false
            },
            onDismiss = { showSheet = false },
        )
    }
}

@Composable
private fun PickerSheet(
    title: String,
    value: String,
    options: List<String>,
    searchHint: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = GarageDesignTheme.colors
    var query by remember { mutableStateOf("") }
    val trimmed = query.trim()
    val filtered = remember(trimmed, options) {
        if (trimmed.isEmpty()) options
        else options.filter { it.contains(trimmed, ignoreCase = true) }
            .sortedBy { if (it.startsWith(trimmed, ignoreCase = true)) 0 else 1 }
    }
    val canAddCustom = trimmed.isNotEmpty() && options.none { it.equals(trimmed, ignoreCase = true) }
    val focus = remember { FocusRequester() }
    val maxListHeight = (LocalConfiguration.current.screenHeightDp * 0.6f).dp

    GarageBottomSheet(onDismissRequest = onDismiss, title = { Text(title) }) {
        GarageTextField(
            value = query,
            onValueChange = { query = it },
            style = GarageTextFieldStyle.Search,
            placeholder = searchHint,
            leading = { Icon(Icons.Rounded.Search, contentDescription = null, Modifier.size(18.dp)) },
            trailing = if (query.isNotEmpty()) {
                {
                    Icon(
                        Icons.Rounded.Cancel, contentDescription = stringResource(R.string.common_close),
                        modifier = Modifier.size(18.dp).clip(RoundedCornerShape(50)).clickable { query = "" },
                    )
                }
            } else null,
            modifier = Modifier.focusRequester(focus),
        )
        Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            GarageSectionHeader(
                stringResource(if (trimmed.isEmpty()) R.string.picker_all else R.string.picker_matches),
                modifier = Modifier.weight(1f),
                trailing = { Text(pluralStringResource(R.plurals.picker_count, filtered.size, filtered.size)) },
            )
        }

        if (canAddCustom) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(GarageDesignTheme.radius.sm))
                    .background(colors.primaryMuted)
                    .clickable(role = Role.Button) { onPick(trimmed) }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.common_add_custom, trimmed), style = GarageDesignTheme.typography.label.copy(color = colors.primary))
                    Text(stringResource(R.string.picker_add_hint), style = GarageDesignTheme.typography.caption)
                }
            }
        }

        if (filtered.isEmpty() && !canAddCustom) {
            GarageEmptyState(Icons.Rounded.SearchOff, stringResource(R.string.picker_empty))
        } else if (filtered.isNotEmpty()) {
            GarageCard(Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                LazyColumn(Modifier.fillMaxWidth().heightIn(max = maxListHeight)) {
                    itemsIndexed(filtered, key = { _, option -> option }) { index, option ->
                        if (index > 0) Box(Modifier.fillMaxWidth().height(GarageDesignTheme.stroke.thin).background(colors.line))
                        PickerRow(
                            text = highlighted(option, trimmed),
                            selected = option.equals(value, ignoreCase = true),
                            onClick = { onPick(option) },
                        )
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
}

@Composable
private fun PickerRow(text: AnnotatedString, selected: Boolean, onClick: () -> Unit) {
    val colors = GarageDesignTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 50.dp)
            .background(if (selected) colors.primaryMuted else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            style = GarageDesignTheme.typography.bodyLarge.copy(color = if (selected) colors.primary else colors.text),
            modifier = Modifier.weight(1f),
        )
        if (selected) Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
    }
}

/** Bolds and tints the part of [text] that matches [query]. */
@Composable
private fun highlighted(text: String, query: String): AnnotatedString {
    val start = if (query.isEmpty()) -1 else text.indexOf(query, ignoreCase = true)
    if (start < 0) return AnnotatedString(text)
    val primary = GarageDesignTheme.colors.primary
    return buildAnnotatedString {
        append(text.substring(0, start))
        withStyle(SpanStyle(color = primary, fontWeight = FontWeight.Bold)) { append(text.substring(start, start + query.length)) }
        append(text.substring(start + query.length))
    }
}
