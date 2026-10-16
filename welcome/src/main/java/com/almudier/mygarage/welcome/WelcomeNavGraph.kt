package com.almudier.mygarage.welcome

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.almudier.mygarage.welcome.api.WelcomeRoute

fun EntryProviderScope<NavKey>.welcomeNavGraph() {
    entry<WelcomeRoute> { WelcomeScreen() }
}
