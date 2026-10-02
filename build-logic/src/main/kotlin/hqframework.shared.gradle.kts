plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

val projectGroup: String by project
group = projectGroup
val projectVersion: String by project
version = projectVersion

kotlin {
    jvmToolchain(17)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

val jUnitVersion: String by project

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:$jUnitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
