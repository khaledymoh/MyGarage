plugins {
    id(libs.plugins.app.feature.get().pluginId)
    id(libs.plugins.app.core.get().pluginId)
}

android { namespace = "com.almudier.mygarage.settings" }

dependencies {
    implementation(projects.settings.api)
    implementation(projects.garage.api)
    implementation(projects.welcome.api)
    implementation(projects.authentication.api)
}
