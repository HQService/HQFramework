plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)
    api(libs.ninja)
    api(libs.snakeyaml)

    testImplementation(libs.junit.parameterizedTest)
    testImplementation(project(":modules:test"))
}