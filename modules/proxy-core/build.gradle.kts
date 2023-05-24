plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    apiModule("global", "core")
    api(libs.kotlinx.coroutines.core)
    api(libs.netty)
}