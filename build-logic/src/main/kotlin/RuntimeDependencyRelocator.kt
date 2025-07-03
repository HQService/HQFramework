import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType

class RuntimeDependencyRelocator : Plugin<Project> {
    override fun apply(target: Project) {}
}

class RelocatedRuntimeScope(
    private val relocateTo: String,
    private val project: Project,
    private val dependencyHandler: DependencyHandler
) {
    fun runtimeOnly(provider: Provider<MinimalExternalModuleDependency>): Dependency? {
        relocateDependency(provider.get().group)
        return dependencyHandler.add("runtimeOnly", provider)
    }

    fun api(provider: Provider<MinimalExternalModuleDependency>): Dependency? {
        relocateDependency(provider.get().group)
        return dependencyHandler.add("api", provider)
    }

    fun implementation(provider: Provider<MinimalExternalModuleDependency>): Dependency? {
        relocateDependency(provider.get().group)
        return dependencyHandler.add("implementation", provider)
    }

    private fun relocateDependency(group: String) {
        project.tasks.withType<ShadowJar>()["shadowJar"].relocate(group, "$relocateTo.$group")
    }
}

fun Project.relocatedRuntimeScope(relocateTo: String, scope: RelocatedRuntimeScope.() -> Unit) {
    scope(RelocatedRuntimeScope(relocateTo, this, this.dependencies))
}