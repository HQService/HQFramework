plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.netty)

    testImplementation(project(":modules:test"))
}