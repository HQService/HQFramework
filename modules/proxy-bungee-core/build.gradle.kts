plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    apiModule("global", "core")
    apiModule("global", "netty")
    apiModule("global", "yaml")
    apiModule("proxy", "core")
    compileOnly(libs.bungeecord.api)
    compileOnly(libs.byte.buddy.agent)
    api(libs.kotlinx.coroutines.core)
    api(libs.koin.core)
    api(libs.netty)
    testImplementation(kotlin("test"))
    testImplementation(libs.bungeecord.api)
}