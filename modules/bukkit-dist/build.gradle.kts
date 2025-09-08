plugins {
    id("hqframework.shared")
    id("hqframework.shadow")
    id("kr.hqservice.resource-generator.bukkit")
}

bukkitResourceGenerator {
    main = "kr.hqservice.framework.HQFrameworkBukkit"
    name = "HQFramework"
    apiVersion = "1.13"
    foliaSupport = true
    libraries = excludedRuntimeDependencies(
        libs.kotlin.stdlib.jdk8,
        libs.kotlin.reflect,
        libs.kotlinx.coroutines.core,
        libs.kotlinx.serialization.json,
        libs.koin.core,
        libs.exposed.core,
        libs.exposed.dao,
        libs.exposed.java.time,
        libs.exposed.jdbc,
        libs.hikaricp,
        libs.sqlite,
        libs.h2,
        libs.netty,
        libs.guava,
        libs.snakeyaml,
        libs.configurate.core,
        libs.configurate.yaml,
        libs.byte.buddy.core,
        libs.byte.buddy.agent,
        libs.quartz,
        libs.adventure.text.serializer.legacy,
    )
}

dependencies {
    compileOnly(libs.spigot.api)

    implementationModule("bukkit", "core")
    implementationModule("bukkit", "command")
    runtimeOnlyModule("global", "core")
    runtimeOnlyModule("global", "netty")
    runtimeOnlyModule("global", "yaml")
    runtimeOnlyModule("bukkit", "inventory")
    runtimeOnlyModule("bukkit", "region")
    runtimeOnlyModule("bukkit", "database")
    runtimeOnlyModule("bukkit", "nms")
    runtimeOnly(project(":nms:legacy"))
    runtimeOnly(project(":nms:V20_6"))
    runtimeOnly(project(":nms:V21"))
    runtimeOnly(project(":nms:V21_3"))
    runtimeOnly(project(":nms:V21_5"))
    runtimeOnly(project(":nms:V21_6"))
    runtimeOnly(project(":nms:V21_7"))

    runtimeOnlyModule("bukkit", "scheduler")

    relocatedRuntimeScope("kr.hqservice.framework.shadow") {
        implementation(libs.bStats)
    }
}

configurations.runtimeClasspath.configure {
    exclude(libs.spigot.api.get().group, libs.spigot.api.get().name)
    exclude(libs.paper.api.get().group, libs.paper.api.get().name)
    exclude(libs.kotlin.stdlib.jdk8.get().group, "*")
}