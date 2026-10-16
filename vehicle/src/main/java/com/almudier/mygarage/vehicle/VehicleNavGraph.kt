package com.almudier.mygarage.vehicle

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.almudier.mygarage.vehicle.api.DashRoute
import com.almudier.mygarage.vehicle.api.InsightsRoute
import com.almudier.mygarage.vehicle.api.LogRoute
import com.almudier.mygarage.vehicle.api.VehicleFormRoute
import com.almudier.mygarage.vehicle.api.VehicleProfileRoute
import com.almudier.mygarage.vehicle.dash.DashScreen
import com.almudier.mygarage.vehicle.insights.InsightsScreen
import com.almudier.mygarage.vehicle.log.LogScreen
import com.almudier.mygarage.vehicle.profile.VehicleProfileScreen

fun EntryProviderScope<NavKey>.vehicleNavGraph() {
    entry<DashRoute> { route -> DashScreen(route) }
    entry<LogRoute> { route -> LogScreen(route) }
    entry<InsightsRoute> { route -> InsightsScreen(route) }
    entry<VehicleProfileRoute> { route -> VehicleProfileScreen(route) }
    entry<VehicleFormRoute> { route -> VehicleFormScreen(route) }
}
