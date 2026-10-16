package com.almudier.mygarage.main

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.almudier.mygarage.core.presentation.navigation.Navigator
import com.almudier.mygarage.vehicle.api.CarHomeRoute

fun EntryProviderScope<NavKey>.mainNavGraph(navigator: Navigator) {
    entry<CarHomeRoute> { route ->
        MainScreen(vehicleId = route.vehicleId, navigator = navigator)
    }
}
