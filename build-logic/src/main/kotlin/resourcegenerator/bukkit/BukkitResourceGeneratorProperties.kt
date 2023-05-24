package resourcegenerator.bukkit

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import resourcegenerator.ResourceGeneratorProperties

@Suppress("unused")
class BukkitResourceGeneratorProperties(project: Project) : ResourceGeneratorProperties {
    @Input
    @Optional
    @JsonProperty("api-version")
    var apiVersion: String? = null
    @Input
    var name: String? = null

    /*
       여기서 project 의 프로퍼티에 접근하면 프로젝트가 initialize 되기전에 접근하므로 기본값인 unspecified 가 되기 때문에,
       Plugin 의 doFirst 에서 해준다.
     */
    @Input
    @Optional
    var version: String? = null
    @Input
    var main: String? = null
    @Input
    @Optional
    var description: String? = null
    @Input
    @Optional
    var load: PluginLoadOrder? = null
    @Input
    @Optional
    var author: String? = null
    @Input
    @Optional
    var authors: List<String>? = null
    @Input
    @Optional
    var website: String? = null
    @Input
    @Optional
    var depend: List<String>? = null
    @Input
    @Optional
    @JsonProperty("softdepend")
    var softDepend: List<String>? = null
    @Input
    @Optional
    @JsonProperty("loadbefore")
    var loadBefore: List<String>? = null
    @Input
    @Optional
    var prefix: String? = null
    @Input
    @Optional
    @JsonProperty("default-permission")
    var defaultPermission: Permission.Default? = null
    @Input
    @Optional
    var provides: List<String>? = null
    @Input
    @Optional
    var libraries: List<Provider<MinimalExternalModuleDependency>>? = null

    @Nested
    val commands: NamedDomainObjectContainer<Command> = project.container(Command::class.java)
    @Nested
    val permissions: NamedDomainObjectContainer<Permission> = project.container(Permission::class.java)

    enum class PluginLoadOrder {
        STARTUP,
        POSTWORLD
    }

    data class Command(@Input @JsonIgnore val name: String) {
        @Input
        @Optional
        var description: String? = null
        @Input
        @Optional
        var aliases: List<String>? = null
        @Input
        @Optional
        var permission: String? = null
        @Input
        @Optional
        @JsonProperty("permission-message")
        var permissionMessage: String? = null
        @Input
        @Optional
        var usage: String? = null
    }

    data class Permission(@Input @JsonIgnore val name: String) {
        @Input
        @Optional
        var description: String? = null
        @Input
        @Optional
        var default: Default? = null
        var children: List<String>?
            @Internal @JsonIgnore get() = childrenMap?.filterValues { it }?.keys?.toList()
            set(value) {
                childrenMap = value?.associateWith { true }
            }
        @Input
        @Optional
        @JsonProperty("children")
        var childrenMap: Map<String, Boolean>? = null

        enum class Default {
            @JsonProperty("true")
            TRUE,
            @JsonProperty("false")
            FALSE,
            @JsonProperty("op")
            OP,
            @JsonProperty("!op")
            NOT_OP
        }
    }
}
