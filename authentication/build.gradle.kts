plugins {
    id(libs.plugins.app.feature.get().pluginId)
    id(libs.plugins.app.core.get().pluginId)
}

android {
    namespace = "com.almudier.mygarage.authentication"
}

dependencies {
    implementation(projects.authentication.api)
    implementation(projects.core.data)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.bundles.credentials)
    implementation(libs.kotlinx.coroutines.play.services)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
