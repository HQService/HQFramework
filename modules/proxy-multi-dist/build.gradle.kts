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
    implementationModule("proxy", "multi-core")
    kapt(libs.velocity.api)

    relocatedRuntimeScope("kr.hqservice.framework.shadow") {
        api(libs.koin.core)

        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.serialization.json)
    }
}