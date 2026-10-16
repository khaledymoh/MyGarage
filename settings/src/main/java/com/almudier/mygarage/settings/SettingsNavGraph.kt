package com.almudier.mygarage.settings

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.almudier.mygarage.settings.api.SettingsRoute

fun EntryProviderScope<NavKey>.settingsNavGraph() {
    entry<SettingsRoute> { SettingsScreen() }
}
