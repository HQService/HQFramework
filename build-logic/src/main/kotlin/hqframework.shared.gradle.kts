import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    kotlin("kapt")
    id("com.google.devtools.ksp")
}

val projectGroup: String by project
group = projectGroup
val projectVersion: String by project
version = projectVersion

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    "ksp"("io.insert-koin:koin-ksp-compiler:${getProperty("koinAnnotationsVersion")}")
}

extensions.configure<KotlinJvmProjectExtension>("kotlin") {
    sourceSets.named("main").configure {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.named("test").configure {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}