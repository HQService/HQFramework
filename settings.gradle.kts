@file:Suppress("UnstableApiUsage")

rootProject.name = "HQFramework"

pluginManagement {
    val kotlinVersion: String by settings
    val shadowVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion apply false
        kotlin("plugin.serialization") version kotlinVersion apply false
        kotlin("kapt") version kotlinVersion apply false
        id("com.github.johnrengelman.shadow") version shadowVersion apply false
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://mvnrepository.com/artifact/io.netty/netty-all")
    }

    versionCatalogs {
        create("libs") {
            library("kotlin-stdlib", "org.jetbrains.kotlin:kotlin-stdlib:${getProperty("kotlinVersion")}")
            library("kotlin-reflect", "org.jetbrains.kotlin:kotlin-reflect:${getProperty("kotlinVersion")}")
            library("spigot-api", "org.spigotmc:spigot-api:${getProperty("spigotVersion")}")

            library("koin-core", "io.insert-koin:koin-core-jvm:${getProperty("koinVersion")}")
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
            library("mockBukkit", "com.github.seeseemelk:MockBukkit-v1.19:${getProperty("mockBukkitVersion")}")
            library("mysql-connector", "mysql:mysql-connector-java:${getProperty("mysqlConnectorVersion")}")
            library("junit-parameterizedTest", "org.junit.jupiter:junit-jupiter-params:${getProperty("jUnitVersion")}")
            library("auto-service", "com.google.auto.service:auto-service:${getProperty("autoServiceVersion")}")
            library("auto-service-annotations", "com.google.auto.service:auto-service-annotations:${getProperty("autoServiceVersion")}")
            library("netty", "io.netty:netty-all:${getProperty("nettyVersion")}")
            library("guava", "com.google.guava:guava:${getProperty("guavaVersion")}")
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