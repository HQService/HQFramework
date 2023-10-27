plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    apiModule("global", "core")
    apiModule("bukkit", "core")
    api(libs.spigot.api)
    api(libs.kotlinx.coroutines.core)
    api(libs.guava)

    testImplementationModule("bukkit", "test")
    testImplementation(libs.mockBukkit)
    testImplementation(libs.koin.core)
    testImplementation(libs.mockK)
    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test)
}