plugins {
    id("hqframework.shared")
}

dependencies {
    apiModule("global", "core")
    apiModule("bukkit", "core")
    apiModule("bukkit", "nms")
    api(libs.paper.api)
    api(libs.kotlinx.coroutines.core)
    api(libs.exposed.core)
    api(libs.netty)
    api(libs.koin.core)

    testImplementationModule("bukkit", "test")
    testImplementation(libs.mockBukkit)
    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.kotlinx.coroutines.test)
}