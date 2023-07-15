plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    apiModule("global", "core")
    apiModule("bukkit", "core")
    apiModule("bukkit", "nms")
    apiModule("bukkit", "coroutine")
    compileOnly(libs.spigot.api)
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockBukkit)
    testImplementation(libs.junit.parameterizedTest)
}