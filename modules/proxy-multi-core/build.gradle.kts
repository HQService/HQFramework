plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    apiModule("global", "core")
    apiModule("global", "yaml")
    apiModule("proxy", "core")
    apiModule("multi", "netty")

    compileOnly(libs.velocity.api)
    compileOnly(libs.byte.buddy.agent)
    compileOnly("com.github.ProxioDev.ValioBungee:RedisBungee-Bungee:0.12.3")

    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)
    api(libs.koin.core)
    api(libs.netty)
}