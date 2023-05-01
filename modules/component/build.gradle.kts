plugins {
    id("hqframework.shared")
}

dependencies {
    compileOnly(libs.spigot.api)
    api(libs.kotlinx.coroutines.core)
}