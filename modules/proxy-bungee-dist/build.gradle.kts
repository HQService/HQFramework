plugins {
    id("hqframework.shared")
    id("hqframework.bungee-resource-generator")
    id("hqframework.shadow")
}

bungeeResourceGenerator {
    main = "kr.hqservice.framework.HQFrameworkBungee"
    name = "HQFramework"
    libraries = excludedRuntimeDependencies(
        libs.kotlin.stdlib.jdk8,
        libs.kotlin.reflect,
        libs.kotlinx.coroutines.core,
        libs.kotlinx.serialization.json,
        libs.koin.core,
        libs.koin.annotations,
        libs.exposed.core,
        libs.exposed.dao,
        libs.exposed.java.time,
        libs.exposed.jdbc,
        libs.hikaricp,
        libs.sqlite,
        libs.netty,
        libs.guava,
        libs.auto.service.annotations,
        libs.snakeyaml,
        libs.configurate.yaml
    )
}

dependencies {
    implementationModule("proxy-bungee", "core")
    runtimeOnlyModule("global", "core")
    compileOnly(libs.bungeecord.api)
}