plugins {
    id("hqframework.shared")
}

dependencies {
    compileOnly(libs.spigot.api)
    api(project(":modules:core"))
    api(libs.kotlinx.coroutines.core)
}