plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    compileOnly(libs.spigot.api)
    apiModule("bukkit", "core")
    apiModule("bukkit", "database")
    apiModule("bukkit", "command")
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)
    api(libs.netty)
    api(libs.guava)
    api(libs.quartz)
    api(libs.exposed.core)

    testImplementationModule("global", "yaml")
    testImplementation(libs.mysql.connector)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.mockBukkit)
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.netty)
}