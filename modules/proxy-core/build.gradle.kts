plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    api(project(":modules:netty-core"))
    apiModule("global", "core")
    apiModule("global", "yaml")
    api(libs.kotlinx.coroutines.core)
    api(libs.netty)
    api(libs.koin.core)

    testImplementation(libs.koin.core)
    testImplementation(libs.mockK)
    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test)
}