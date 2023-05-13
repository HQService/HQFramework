plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    compileOnly(libs.spigot.api)
    api(libs.kotlinx.coroutines.core)
    api(libs.exposed.core)
    api(libs.exposed.dao)
    api(libs.exposed.jdbc)
    api(libs.exposed.java.time)
    api(libs.hikaricp)
    api(project(":modules:core"))
    api(project(":modules:coroutine"))

    testImplementation(libs.mysql.connector)
    testImplementation(libs.sqlite)
    testImplementation(libs.mockBukkit)
    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(project(":modules:test"))
}