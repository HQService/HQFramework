plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    kotlin("kapt")
}

val projectGroup: String by project
group = projectGroup
val projectVersion: String by project
version = projectVersion

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}