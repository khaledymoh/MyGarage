plugins {
    id(libs.plugins.app.feature.get().pluginId)
    id(libs.plugins.app.core.get().pluginId)
}

android { namespace = "com.almudier.mygarage.vehicle" }

dependencies {
    implementation(libs.coil.compose)
    implementation(projects.vehicle.api)
    implementation(projects.maintenance.api)
    implementation(projects.garage.api)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)
}
