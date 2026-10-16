import com.android.build.api.dsl.ApplicationExtension
import com.almudier.mygarage.build_logic.convention.alias
import com.almudier.mygarage.build_logic.convention.bundleImplementation
import com.almudier.mygarage.build_logic.convention.coreLibraryDesugaring
import com.almudier.mygarage.build_logic.convention.implementation
import com.almudier.mygarage.build_logic.convention.ksp
import com.almudier.mygarage.build_logic.convention.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                alias(libs.plugins.android.application)
                alias(libs.plugins.kotlin.compose)
                alias(libs.plugins.ksp)
                alias(libs.plugins.hilt)
                alias(libs.plugins.kotlin.serialization)
                alias(libs.plugins.kotlin.parcelize)
            }
            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                    freeCompilerArgs.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
                }
            }
            extensions.configure<ApplicationExtension> {
                compileSdk = libs.versions.compileSdk.get().toInt()
                defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
                buildFeatures {
                    buildConfig = true
                    compose = true
                }
                compileOptions {
                    isCoreLibraryDesugaringEnabled = true
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
                packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
            dependencies {
                "implementation"(project(":resources"))
                bundleImplementation(libs.bundles.androidx)
                bundleImplementation(libs.bundles.navigation3)
                bundleImplementation(libs.bundles.hilt)
                implementation(libs.kotlinx.serialization.json)
                implementation(platform(libs.androidx.compose.bom))
                ksp(libs.hilt.compiler)
                coreLibraryDesugaring(libs.desugar.jdk.libs)
            }
        }
    }
}
