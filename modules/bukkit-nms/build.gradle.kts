plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    apiModule("global", "core")
    apiModule("bukkit", "core")
    api(libs.spigot.api)
    api(libs.kotlinx.coroutines.core)
    api(libs.exposed.core)
    api(libs.netty)
    api(libs.koin.core)

    testImplementationModule("bukkit", "test")
    testImplementation(libs.mockBukkit)
    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.kotlinx.coroutines.test)
}