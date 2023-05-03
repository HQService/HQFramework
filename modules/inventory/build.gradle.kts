plugins {
    id("hqframework.shared")
}

dependencies {
    compileOnly(libs.spigot.api)
    api(libs.kotlinx.coroutines.core)
    api(project(":modules:core"))

    testImplementation(libs.mockBukkit)
    testImplementation(libs.junit.parameterizedTest)
}