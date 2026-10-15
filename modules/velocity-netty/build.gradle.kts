plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    api(project(":modules:netty-core"))
    apiModule("global", "core")
    apiModule("global", "yaml")
    implementation(libs.byte.buddy.agent)
    compileOnly(libs.velocity.api)

    api(libs.koin.core)
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)
    api(libs.netty)
    api(libs.guava)
}