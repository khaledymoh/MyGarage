package com.almudier.mygarage.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val uid: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isSignedIn: Boolean = false,
    /** The car picked in the garage; Dash, Log and Insights show this car. */
    val selectedVehicleId: String? = null,
    val currency: String = "JOD",
    /** User overrides of default service-interval lengths, by interval id. */
    val intervalMonths: Map<String, Int> = emptyMap(),
)
