import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    `kotlin-dsl`
    `maven-publish`
}

val properties = loadProperties(rootProject.gradle.parent?.rootProject?.projectDir?.path + "/gradle.properties")
val kotlinVersion = properties.getProperty("kotlinVersion")
val shadowPluginVersion = properties.getProperty("shadowVersion")

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:$shadowPluginVersion")
    api(gradleApi())
}

gradlePlugin {
    plugins {
        register("hqframework-dependency-handler-extensions") {
            id = "hqframework.dependency-handler-extensions"
            implementationClass = "DependencyHandlerExtensions"
        }
        register("hqframework-runtime-dependency-relocator") {
            id = "hqframework.runtime-dependency-relocator"
            implementationClass = "RuntimeDependencyRelocator"
        }
    }
}