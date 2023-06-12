plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    apiModule("global", "core")
    apiModule("global", "yaml")
    apiModule("proxy", "core")
    compileOnly(libs.velocity.api)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)
    api(libs.koin.core)
    api(libs.koin.annotations)
    ksp(libs.koin.ksp.compiler)
    api(libs.netty)
}