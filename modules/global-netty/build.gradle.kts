plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    api(project(":modules:netty-core"))
    apiModule("global", "core")
    apiModule("global", "yaml")
    compileOnly(libs.bungeecord.api)

    api(libs.koin.core)
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)
    api(libs.netty)
    api(libs.guava)

    testImplementationModule("global", "yaml")
    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.mockBukkit)
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.netty)
    testImplementation(libs.bungeecord.api)
    testImplementation(libs.byte.buddy.core)
}