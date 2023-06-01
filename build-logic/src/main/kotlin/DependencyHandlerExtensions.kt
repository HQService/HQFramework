
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

class DependencyHandlerExtensions : Plugin<Project> {
    override fun apply(target: Project) {}
}

fun DependencyHandler.runtimeOnlyModule(platform: String, module: String): Dependency? =
    add("runtimeOnly", project(":modules:${platform}-$module"))

fun DependencyHandler.implementationModule(platform: String, module: String): Dependency? =
    add("implementation", project(":modules:${platform}-$module"))

fun DependencyHandler.apiModule(platform: String, module: String): Dependency? =
    add("api", project(":modules:${platform}-$module"))

fun DependencyHandler.testImplementationModule(platform: String, module: String): Dependency? =
    add("testImplementation", project(":modules:${platform}-$module"))
