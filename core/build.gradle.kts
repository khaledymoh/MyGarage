import com.android.build.api.dsl.LibraryExtension

plugins {
    id(libs.plugins.app.android.library.get().pluginId)
}

extensions.configure<LibraryExtension> {
    namespace = "com.almudier.mygarage.core"
}
