import com.android.build.api.dsl.LibraryExtension

plugins {
    id(libs.plugins.app.android.library.get().pluginId)
}

extensions.configure<LibraryExtension> {
    namespace = "com.almudier.mygarage.core.data"
}

dependencies {
    implementation(projects.core.domain)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.datastore)
    implementation(libs.kotlinx.serialization.json)
}
