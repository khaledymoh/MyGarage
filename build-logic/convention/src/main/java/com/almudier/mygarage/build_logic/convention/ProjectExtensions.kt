package com.almudier.mygarage.build_logic.convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.plugins.PluginManager
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderConvertible
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.plugin.use.PluginDependency
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

val Project.libs: LibrariesForLibs
    get() = extensions["libs"] as LibrariesForLibs

fun PluginManager.alias(notation: Provider<PluginDependency>) {
    apply(notation.get().pluginId)
}

fun PluginManager.alias(notation: ProviderConvertible<PluginDependency>) {
    apply(notation.asProvider().get().pluginId)
}

fun DependencyHandlerScope.implementation(lib: Provider<MinimalExternalModuleDependency>) {
    add("implementation", lib)
}

fun DependencyHandlerScope.bundleImplementation(lib: Provider<ExternalModuleDependencyBundle>) {
    add("implementation", lib)
}

fun DependencyHandlerScope.ksp(lib: Provider<MinimalExternalModuleDependency>) {
    add("ksp", lib)
}

fun DependencyHandlerScope.coreLibraryDesugaring(lib: Provider<MinimalExternalModuleDependency>) {
    add("coreLibraryDesugaring", lib)
}

@Suppress("UnstableApiUsage")
fun Project.appGradleConfiguration(): Project {
    pluginManager.apply(libs.plugins.android.library.get().pluginId)
    pluginManager.apply(libs.plugins.ksp.get().pluginId)
    pluginManager.apply(libs.plugins.kotlin.serialization.get().pluginId)
    pluginManager.apply(libs.plugins.kotlin.parcelize.get().pluginId)
    pluginManager.withPlugin("com.android.library") {
        pluginManager.apply(libs.plugins.hilt.get().pluginId)
    }

    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
        }
    }

    extensions.configure<LibraryExtension> {
        compileSdk = libs.versions.compileSdk.get().toInt()
        defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
        buildFeatures { buildConfig = true }
        compileOptions {
            isCoreLibraryDesugaringEnabled = true
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        buildTypes {
            debug {}
            release {}
        }
    }

    dependencies {
        "implementation"(project(":resources"))
        bundleImplementation(libs.bundles.hilt)
        ksp(libs.hilt.compiler)
        coreLibraryDesugaring(libs.desugar.jdk.libs)
    }
    return this
}
