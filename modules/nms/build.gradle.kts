plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    compileOnly(libs.spigot.api)
    api(libs.kotlinx.coroutines.core)
    api(libs.exposed.core)
    api(project(":modules:core"))

    testImplementation(libs.mockBukkit)
    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(project(":modules:test"))
}