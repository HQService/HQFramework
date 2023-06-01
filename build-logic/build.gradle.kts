import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    `kotlin-dsl`
    `maven-publish`
}

val properties = loadProperties(rootProject.gradle.parent?.rootProject?.projectDir?.path + "/gradle.properties")
val kotlinVersion = properties.getProperty("kotlinVersion")
val kspVersion = properties.getProperty("kspVersion")
val shadowPluginVersion = properties.getProperty("shadowVersion")

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:$kspVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.2")
    implementation("com.github.johnrengelman.shadow:com.github.johnrengelman.shadow.gradle.plugin:$shadowPluginVersion")
    api(gradleApi())
}

gradlePlugin {
    plugins {
        register("hqframework-bukkit-resource-generator") {
            id = "hqframework.bukkit-resource-generator"
            implementationClass = "resourcegenerator.bukkit.BukkitResourceGeneratorPlugin"
        }
        register("hqframework-bungee-resource-generator") {
            id = "hqframework.bungee-resource-generator"
            implementationClass = "resourcegenerator.bungee.BungeeResourceGeneratorPlugin"
        }
        register("hqframework-dependency-handler-extensions") {
            id = "hqframework.dependency-handler-extensions"
            implementationClass = "DependencyHandlerExtensions"
        }
        register("hqframework-configure-extensions") {
            id = "hqframework.configure-extensions"
            implementationClass = "ConfigureExtensions"
        }
        register("hqframework-runtime-dependency-relocator") {
            id = "hqframework.runtime-dependency-relocator"
            implementationClass = "RuntimeDependencyRelocator"
        }
    }
}