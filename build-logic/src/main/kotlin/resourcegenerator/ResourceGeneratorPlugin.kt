package resourcegenerator

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

abstract class ResourceGeneratorPlugin<T : ResourceGeneratorProperties>(private val platformName: String, private val fileName: String = "plugin.yml") : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        val properties = provideResourceGeneratorProperties(this)

        extensions.add("${platformName}ResourceGenerator", properties)

        val generatedResourcesDirectory = layout.buildDirectory.dir("generated/resource-generator/$platformName")

        val generateTask = tasks.register<ResourceGeneratorTask>("generate${platformName.replaceFirstChar { it.uppercase() }}Resource") {
            fileName.set(this@ResourceGeneratorPlugin.fileName)
            outputDirectory.set(generatedResourcesDirectory)
            pluginResource.set(provider {
                properties.apply {
                    setDefaults(project, this)
                }
            })
        }


        plugins.withType<JavaPlugin> {
            extensions.getByType<SourceSetContainer>().named(SourceSet.MAIN_SOURCE_SET_NAME) {
                resources.srcDir(generateTask)
                configurations.getByName(compileOnlyConfigurationName)
            }
        }
    }

    protected abstract fun provideResourceGeneratorProperties(project: Project): T
    protected abstract fun setDefaults(project: Project, properties: T)
}