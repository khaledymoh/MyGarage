package com.almudier.mygarage.garage

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.almudier.mygarage.garage.api.GarageRoute

fun EntryProviderScope<NavKey>.garageNavGraph() {
    entry<GarageRoute> {
        GarageScreen()
    }
}
