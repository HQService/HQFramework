package resourcegenerator.bukkit

import org.gradle.api.Project
import resourcegenerator.ResourceGeneratorPlugin

@Suppress("unused")
class BukkitResourceGeneratorPlugin : ResourceGeneratorPlugin<BukkitResourceGeneratorProperties>("bukkit") {
    override fun setDefaults(project: Project, properties: BukkitResourceGeneratorProperties) {
        properties.name = properties.name ?: project.name
        properties.version = properties.version ?: project.version.toString()
        properties.description = properties.description ?: project.description
        properties.website = properties.website ?: project.findProperty("projectUrl")?.toString()
        properties.author = properties.author ?: project.findProperty("projectAuthors")?.toString()
    }

    override fun provideResourceGeneratorProperties(project: Project): BukkitResourceGeneratorProperties {
        return BukkitResourceGeneratorProperties(project)
    }
}
