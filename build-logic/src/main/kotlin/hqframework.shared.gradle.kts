import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
}

val projectGroup: String by project
group = projectGroup
val projectVersion: String by project
version = projectVersion

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

extensions.configure<KotlinJvmProjectExtension>("kotlin") {
    sourceSets.named("main").configure {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.named("test").configure {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}