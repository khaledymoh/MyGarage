import com.almudier.mygarage.build_logic.convention.appGradleConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.appGradleConfiguration()
    }
}
