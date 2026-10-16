package com.almudier.mygarage.maintenance.api
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
/** [type] preselects the record type for a new record (a `RecordType` name); ignored when editing. */
@Serializable data class MaintenanceFormRoute(val vehicleId: String, val recordId: String? = null, val type: String? = null) : NavKey
@Serializable data class MaintenanceDetailRoute(val vehicleId: String, val recordId: String) : NavKey
