import org.gradle.initialization.DependenciesAccessors
import org.gradle.kotlin.dsl.support.serviceOf

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    compileOnly(libs.gradle)

    gradle.serviceOf<DependenciesAccessors>().classes.asFiles.forEach {
        compileOnly(files(it.absolutePath))
    }
}

gradlePlugin {
    plugins {
        register("appAndroidLibrary") {
            id = libs.plugins.app.android.library.get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("appFeature") {
            id = libs.plugins.app.feature.get().pluginId
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("appCore") {
            id = libs.plugins.app.core.get().pluginId
            implementationClass = "AndroidCoreConventionPlugin"
        }
        register("appApplication") {
            id = libs.plugins.app.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
    }
}
