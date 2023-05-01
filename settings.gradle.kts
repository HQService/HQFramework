@file:Suppress("UnstableApiUsage")

rootProject.name = "HQFramework"

pluginManagement {
    val kotlinVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion apply false
        kotlin("plugin.serialization") version kotlinVersion apply false
        id("com.vjh0107.bukkit-resource-generator") version "1.0.1" apply false
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    versionCatalogs {
        create("libs") {
            library("spigot-api", "org.spigotmc:spigot-api:${getProperty("spigotVersion")}")

            library("koin-core", "io.insert-koin:koin-core-jvm:${getProperty("koinVersion")}")
            library("koin-core-coroutines", "io.insert-koin:koin-core-coroutines-jvm:${getProperty("koinVersion")}")
            library("koin-annotations", "io.insert-koin:koin-annotations-jvm:${getProperty("koinAnnotationsVersion")}")
            library("koin-ksp-compiler", "io.insert-koin:koin-ksp-compiler:${getProperty("koinAnnotationsVersion")}")
            library("koin-test", "io.insert-koin:koin-test-jvm:${getProperty("koinVersion")}")

            library("exposed-core", "org.jetbrains.exposed:exposed-core:${getProperty("exposedVersion")}")
            library("exposed-dao", "org.jetbrains.exposed:exposed-dao:${getProperty("exposedVersion")}")
            library("exposed-jdbc", "org.jetbrains.exposed:exposed-jdbc:${getProperty("exposedVersion")}")
            library("exposed-java-time", "org.jetbrains.exposed:exposed-java-time:${getProperty("exposedVersion")}")

            library("kotlinx-coroutines-core", "org.jetbrains.kotlinx:kotlinx-coroutines-core:${getProperty("coroutinesVersion")}")
            library("kotlinx-coroutines-test", "org.jetbrains.kotlinx:kotlinx-coroutines-test:${getProperty("coroutinesVersion")}")
            library("kotlinx-serialization-json", "org.jetbrains.kotlinx:kotlinx-serialization-json:${getProperty("serializationVersion")}")

            library("hikaricp", "com.zaxxer:HikariCP:${getProperty("hikariCPVersion")}")
            library("sqlite", "org.xerial:sqlite-jdbc:${getProperty("sqliteVersion")}")

            library("mockK", "io.mockk:mockk:${getProperty("mockKVersion")}")
            library("mockBukkit", "com.github.MockBukkit:MockBukkit:${getProperty("mockBukkitVersion")}")
            library("mysql-connector", "mysql:mysql-connector-java:${getProperty("mysqlConnectorVersion")}")
            library("junit-parameterizedTest", "org.junit.jupiter:junit-jupiter-params:${getProperty("jUnitVersion")}")
        }
    }
}

includeBuild("build-logic")
includeAll("modules")

fun includeAll(modulesDir: String) {
    file("${rootProject.projectDir.path}/${modulesDir.replace(":", "/")}/").listFiles()?.forEach { modulePath ->
        include("${modulesDir.replace("/", ":")}:${modulePath.name}")
    }
}

fun getProperty(key: String): String {
    return extra[key]?.toString() ?: throw IllegalArgumentException("property with $key is null")
}
