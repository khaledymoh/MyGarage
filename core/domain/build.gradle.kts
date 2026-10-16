plugins {
    id(libs.plugins.app.android.library.get().pluginId)
}

android {
    namespace = "com.almudier.mygarage.core.domain"
}

dependencies {
    testImplementation(libs.junit)
}
