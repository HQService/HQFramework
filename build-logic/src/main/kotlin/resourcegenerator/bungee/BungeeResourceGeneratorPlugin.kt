package resourcegenerator.bungee

import org.gradle.api.Project
import resourcegenerator.ResourceGeneratorPlugin

@Suppress("unused")
class BungeeResourceGeneratorPlugin : ResourceGeneratorPlugin<BungeeResourceGeneratorProperties>("bungee", "bungee.yml") {
    override fun setDefaults(project: Project, properties: BungeeResourceGeneratorProperties) {
        properties.name = properties.name ?: project.name
        properties.version = properties.version ?: project.version.toString()
        properties.description = properties.description ?: project.description
        properties.author = properties.author ?: project.findProperty("projectAuthors")?.toString()
    }

    override fun provideResourceGeneratorProperties(project: Project): BungeeResourceGeneratorProperties {
        return BungeeResourceGeneratorProperties()
    }
}
