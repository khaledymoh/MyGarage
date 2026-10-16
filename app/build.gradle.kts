import java.util.Properties

plugins {
    id(libs.plugins.app.android.application.get().pluginId)
    id(libs.plugins.app.core.get().pluginId)
    id(libs.plugins.google.services.get().pluginId)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}
// Only sign when keystore.properties points at a real keystore file (CI writes both from secrets).
val keystoreFile = keystoreProperties.getProperty("keystoreFile")?.let { rootProject.file(it) }?.takeIf { it.isFile }

android {
    namespace = "com.almudier.mygarage"
    defaultConfig {
        applicationId = "com.almudier.mygarage"
        targetSdk = 36
        // CI passes -PversionCode / -PversionName for each release; local builds stay 1 / "1.0".
        versionCode = providers.gradleProperty("versionCode").orNull?.toInt() ?: 1
        versionName = providers.gradleProperty("versionName").orNull ?: "1.0"
    }
    signingConfigs {
        if (keystoreFile != null) {
            create("release") {
                storeFile = keystoreFile
                storePassword = keystoreProperties["keystorePassword"].toString()
                keyAlias = keystoreProperties["keyAlias"].toString()
                keyPassword = keystoreProperties["keyPassword"].toString()
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            if (keystoreFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.config)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(projects.core.data)
    implementation(projects.authentication)
    implementation(projects.authentication.api)
    implementation(projects.welcome)
    implementation(projects.welcome.api)
    implementation(projects.garage)
    implementation(projects.garage.api)
    implementation(projects.vehicle)
    implementation(projects.vehicle.api)
    implementation(projects.maintenance)
    implementation(projects.maintenance.api)
    implementation(projects.settings)
    implementation(projects.settings.api)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.glance.appwidget)
    implementation(projects.core.domain)
    implementation(projects.core.presentation)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
