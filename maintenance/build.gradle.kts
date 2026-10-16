plugins {
    id(libs.plugins.app.feature.get().pluginId)
    id(libs.plugins.app.core.get().pluginId)
}

android { namespace = "com.almudier.mygarage.maintenance" }

dependencies {
    implementation(projects.maintenance.api)
    implementation(projects.vehicle.api)
}
