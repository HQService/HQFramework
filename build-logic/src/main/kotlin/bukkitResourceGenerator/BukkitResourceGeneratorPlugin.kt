package bukkitResourceGenerator

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.register
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

@Suppress("unused")
class BukkitResourceGeneratorPlugin : Plugin<Project> {
    private fun createConfiguration(project: Project): Configuration {
        val library = project.configurations.maybeCreate("library")
        return project.configurations.create("bukkitLibrary").extendsFrom(library)
    }

    override fun apply(project: Project) {
        project.run {
            val properties = BukkitResourceGeneratorProperties(project)

            extensions.add("bukkitResourceGenerator", properties)

            val generatedResourcesDirectory = layout.buildDirectory.dir("generated/bukkitResourceGenerator/bukkit")

            val libraries = createConfiguration(this)

            val generateTask = tasks.register<BukkitResourceGeneratorTask>("generateBukkitPluginDescription") {
                fileName.set("plugin.yml")
                outputDirectory.set(generatedResourcesDirectory)
                pluginResource.set(provider {
                    properties.apply {
                        name = properties.name ?: project.name
                        version = properties.version ?: project.version.toString()
                        this.description = properties.description ?: project.description
                        website = properties.website ?: project.findProperty("url")?.toString()
                        author = properties.author ?: project.findProperty("author")?.toString()
                    }
                })

                doFirst {
                    with(properties) {
                        this.libraries = properties.libraries ?: libraries.resolvedConfiguration.firstLevelModuleDependencies
                            .map { it.module.id.toString() }
                        if (name.isNullOrEmpty()) throw NullPointerException("플러그인의 이름을 명시하여 주세요.")
                        if (main.isNullOrEmpty()) throw NullPointerException("플러그인의 메인 클래스를 명시하여 주세요.")
                    }
                }
            }

            plugins.withType<JavaPlugin> {
                extensions.getByType<SourceSetContainer>().named(SourceSet.MAIN_SOURCE_SET_NAME) {
                    resources.srcDir(generateTask)
                    configurations.getByName(compileOnlyConfigurationName).extendsFrom(libraries)
                }
            }
        }
    }
}
