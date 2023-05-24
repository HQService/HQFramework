package resourcegenerator.bungee

import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import resourcegenerator.ResourceGeneratorProperties

class BungeeResourceGeneratorProperties : ResourceGeneratorProperties {
    @Input
    var name: String? = null
    @Input
    var main: String? = null
    @Input
    @Optional
    var version: String? = null
    @Input
    @Optional
    var author: String? = null
    @Input
    @Optional
    var depends: Set<String>? = null
    @Input
    @Optional
    var softDepends: Set<String>? = null
    @Input
    @Optional
    var description: String? = null
    @Input
    @Optional
    var libraries: List<Provider<MinimalExternalModuleDependency>>? = null
}