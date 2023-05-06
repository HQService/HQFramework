plugins {
    id("hqframework.shared")
    id("hqframework.bukkit-resource-generator")
    id("hqframework.shadow")
}

bukkitResourceGenerator {
    main = "kr.hqservice.framework.HQFrameworkPlugin"
    name = "HQFramework"
    apiVersion = "1.13"
    website = "https://github.com/HighQualityService"
    libraries = listOf(
        libs.kotlin.stdlib.get().toString(),
        libs.kotlin.reflect.get().toString(),
        libs.kotlinx.coroutines.core.get().toString(),
        libs.kotlinx.serialization.json.get().toString(),
        libs.koin.core.get().toString(),
        libs.koin.annotations.get().toString(),
        libs.exposed.core.get().toString(),
        libs.exposed.dao.get().toString(),
        libs.exposed.java.time.get().toString(),
        libs.exposed.jdbc.get().toString(),
        libs.hikaricp.get().toString(),
        libs.sqlite.get().toString()
    )
}

apply(file(project.projectDir.path + "/exclude.gradle.kts"))

val excludeSet = org.gradle.internal.Cast.uncheckedNonnullCast<List<Pair<String, String>>>(extra["excludeSet"]!!)
for ((group, module) in excludeSet) {
    configurations.runtimeClasspath.get().exclude(group, module)
}

tasks.shadowJar {
    includeModules("inventory", "item", "region")
    destinationDirectory.set(file(rootProject.projectDir.path + "/build_outputs"))
}

fun includeModules(vararg modules: String) {
    configurations.runtimeClasspath.configure {
        dependencies {
            api(project(":modules:core"))
            modules.forEach {
                api(project(":modules:$it"))
            }
        }
    }
}