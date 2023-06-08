plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    apiModule("global", "core")
    apiModule("global", "netty")
    apiModule("global", "yaml")
    compileOnly(libs.spigot.api)
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)
    api(libs.koin.core)
    api(libs.koin.annotations)
    api(libs.kotlinx.serialization.json)
    ksp(libs.koin.ksp.compiler)
    kapt(libs.auto.service)
    api(libs.auto.service.annotations)

    testImplementationModule("bukkit", "test")
    testImplementation(libs.mockBukkit)
    testImplementation(libs.snakeyaml)
    testImplementation(libs.koin.core)
    testImplementation(libs.koin.annotations)
    testImplementation(libs.mockK)
    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test)
}