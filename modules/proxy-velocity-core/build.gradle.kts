plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    apiModule("global", "core")
    apiModule("global", "yaml")
    apiModule("proxy", "core")
    apiModule("velocity", "netty")

    compileOnly(libs.velocity.api)
    compileOnly(libs.byte.buddy.agent)

    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)
    api(libs.koin.core)
    api(libs.netty)
}