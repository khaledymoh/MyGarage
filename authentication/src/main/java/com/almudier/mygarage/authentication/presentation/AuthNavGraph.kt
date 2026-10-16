package com.almudier.mygarage.authentication.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.almudier.mygarage.authentication.api.AuthRoute

fun EntryProviderScope<NavKey>.authNavGraph(onAuthenticated: () -> Unit) {
    entry<AuthRoute> { AuthScreen(onAuthenticated = onAuthenticated) }
}
