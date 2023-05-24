plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    apiModule("global", "core")
    apiModule("proxy", "core")
    compileOnly(libs.bungeecord.api)
    api(libs.kotlinx.coroutines.core)
    api(libs.koin.core)
    api(libs.koin.annotations)
    ksp(libs.koin.ksp.compiler)
    api(libs.netty)
}