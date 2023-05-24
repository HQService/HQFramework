plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    apiModule("global", "core")
    apiModule("bukkit", "core")
    compileOnly(libs.spigot.api)
    api(libs.kotlinx.coroutines.core)
    kapt(libs.auto.service)
    api(libs.auto.service.annotations)

    testImplementationModule("global", "core")
    testImplementationModule("bukkit", "core")
    testImplementationModule("bukkit", "test")
    testImplementation(libs.mockBukkit)
    testImplementation(libs.mockK)
    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.kotlinx.coroutines.test)
}