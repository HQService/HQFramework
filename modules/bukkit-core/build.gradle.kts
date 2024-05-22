plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    apiModule("global", "core")
    apiModule("global", "netty")
    apiModule("global", "yaml")
    api(libs.spigot.api)
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)
    api(libs.koin.core)
    implementation(libs.byte.buddy.agent)
    api(libs.kotlinx.serialization.json)
    implementation(libs.netty)
    compileOnly(libs.vault.api)

    testImplementationModule("bukkit", "test")
    testImplementation(libs.mockBukkit)
    testImplementation(libs.snakeyaml)
    testImplementation(libs.koin.core)
    testImplementation(libs.mockK)
    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test)
}