plugins {
    id(libs.plugins.app.feature.get().pluginId)
}

android {
    namespace = "com.almudier.mygarage.core.presentation"
}

dependencies {
    implementation(projects.core.domain)
    implementation(libs.haze)
    implementation(libs.coil.compose)
    implementation(libs.coil.network)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
