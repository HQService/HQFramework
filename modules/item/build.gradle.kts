plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    compileOnly(libs.spigot.api)
    api(project(":modules:core"))
    api(libs.kotlinx.coroutines.core)
}