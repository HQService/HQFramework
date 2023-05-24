plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    apiModule("global", "core")
    apiModule("bukkit", "core")
    apiModule("bukkit", "coroutine")
    compileOnly(libs.spigot.api)
    api(libs.kotlinx.coroutines.core)
    api(libs.exposed.core)
    api(libs.exposed.dao)
    api(libs.exposed.jdbc)
    api(libs.exposed.java.time)
    api(libs.hikaricp)

    testImplementationModule("bukkit", "test")
    testImplementation(libs.mysql.connector)
    testImplementation(libs.sqlite)
    testImplementation(libs.mockBukkit)
    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.kotlinx.coroutines.test)
}