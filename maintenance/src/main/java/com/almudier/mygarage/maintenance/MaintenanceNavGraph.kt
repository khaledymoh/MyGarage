package com.almudier.mygarage.maintenance

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.almudier.mygarage.maintenance.api.MaintenanceDetailRoute
import com.almudier.mygarage.maintenance.api.MaintenanceFormRoute

fun EntryProviderScope<NavKey>.maintenanceNavGraph() {
    entry<MaintenanceDetailRoute> { route -> MaintenanceDetailScreen(route) }
    entry<MaintenanceFormRoute> { route -> MaintenanceFormScreen(route) }
}
