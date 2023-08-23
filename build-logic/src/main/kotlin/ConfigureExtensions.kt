import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.named

class ConfigureExtensions : Plugin<Project> {
    override fun apply(target: Project) {}
}

fun Project.excludedRuntimeDependencies(vararg dependencyProviders: Provider<MinimalExternalModuleDependency>): List<Provider<MinimalExternalModuleDependency>> {
    for (dependency in dependencyProviders) {
        println("excluded: ${dependency.get().group}, ${dependency.get().name}")
        configurations.named<org.gradle.api.artifacts.Configuration>("runtimeClasspath").get().exclude(dependency.get().group, dependency.get().name)
    }
    return dependencyProviders.toList()
}

fun Project.getProperty(key: String): String? {
    return extra[key]?.toString()
}