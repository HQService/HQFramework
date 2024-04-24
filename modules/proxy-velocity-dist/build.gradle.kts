plugins {
    id("hqframework.shared")
    id("hqframework.shadow")
    id("hqframework.runtime-dependency-relocator")
    kotlin("kapt")
}

dependencies {
    compileOnly(libs.velocity.api)
    implementationModule("global", "core")
    implementationModule("proxy", "core")
    implementationModule("proxy", "velocity-core")
    kapt(libs.velocity.api)
    relocatedRuntimeScope("kr.hqservice.framework.shadow") {
        implementation(libs.kotlinx.coroutines.core)
        api(libs.koin.core)
        //implementation(libs.netty)
        implementation(libs.kotlinx.serialization.json)
    }
}