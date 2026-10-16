plugins {
    id(libs.plugins.app.feature.get().pluginId)
    id(libs.plugins.app.core.get().pluginId)
}

android { namespace = "com.almudier.mygarage.welcome" }

dependencies {
    implementation(projects.welcome.api)
    implementation(projects.vehicle.api)
}
