package com.almudier.mygarage.vehicle.api
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
@Serializable data class VehicleFormRoute(val vehicleId: String? = null) : NavKey

/** The picked car's home: Dash · Log · (+) · Insights · Settings tabs. */
@Serializable data class CarHomeRoute(val vehicleId: String) : NavKey

/** Tab roots inside [CarHomeRoute]. */
@Serializable data class DashRoute(val vehicleId: String) : NavKey
@Serializable data class LogRoute(val vehicleId: String) : NavKey
@Serializable data class InsightsRoute(val vehicleId: String) : NavKey

/** The car's identity: photo, specs, edit and delete. */
@Serializable data class VehicleProfileRoute(val vehicleId: String) : NavKey
