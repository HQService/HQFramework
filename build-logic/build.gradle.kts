import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    `kotlin-dsl`
}

val properties = loadProperties(rootProject.gradle.parent?.rootProject?.projectDir?.path + "/gradle.properties")
val kotlinVersion = properties.getProperty("kotlinVersion")
val kspVersion = properties.getProperty("kspVersion")

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:$kspVersion")
}