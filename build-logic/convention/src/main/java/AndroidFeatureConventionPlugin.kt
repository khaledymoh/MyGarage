import com.android.build.api.dsl.LibraryExtension
import com.almudier.mygarage.build_logic.convention.alias
import com.almudier.mygarage.build_logic.convention.appGradleConfiguration
import com.almudier.mygarage.build_logic.convention.bundleImplementation
import com.almudier.mygarage.build_logic.convention.implementation
import com.almudier.mygarage.build_logic.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target.appGradleConfiguration()) {
            pluginManager.apply {
                alias(libs.plugins.kotlin.compose)
                alias(libs.plugins.kotlin.serialization)
            }
            extensions.configure<LibraryExtension> {
                buildFeatures { compose = true }
            }
            dependencies {
                bundleImplementation(libs.bundles.androidx)
                bundleImplementation(libs.bundles.navigation3)
                implementation(libs.kotlinx.serialization.json)
                implementation(platform(libs.androidx.compose.bom))
            }
        }
    }
}
