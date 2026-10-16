plugins {
    id(libs.plugins.app.feature.get().pluginId)
    id(libs.plugins.app.core.get().pluginId)
}

android { namespace = "com.almudier.mygarage.garage" }

dependencies {
    implementation(projects.garage.api)
    implementation(projects.vehicle.api)
    implementation(projects.settings.api)
}
