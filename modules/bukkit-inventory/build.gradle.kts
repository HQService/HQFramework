plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    apiModule("global", "core")
    apiModule("bukkit", "core")
    apiModule("bukkit", "nms")
    api(libs.spigot.api)
    api(libs.kotlinx.coroutines.core)
    api(libs.adventure.text.serializer.legacy)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockBukkit)
    testImplementation(libs.junit.parameterizedTest)
}