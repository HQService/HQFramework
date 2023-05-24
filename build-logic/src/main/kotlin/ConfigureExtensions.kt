import gradle.kotlin.dsl.accessors._db863da26cb404e0ebee710166f16934.runtimeClasspath
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.exclude

class ConfigureExtensions : Plugin<Project> {
    override fun apply(target: Project) {}
}

fun Project.excludedRuntimeDependencies(vararg dependencyProviders: Provider<MinimalExternalModuleDependency>): List<Provider<MinimalExternalModuleDependency>> {
    for (dependency in dependencyProviders) {
        println("excluded: ${dependency.get().group}, ${dependency.get().name}")
        configurations.runtimeClasspath.get().exclude(dependency.get().group, dependency.get().name)
    }
    return dependencyProviders.toList()
}