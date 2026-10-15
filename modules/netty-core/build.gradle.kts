plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    apiModule("global", "core")
    apiModule("global", "yaml")
    implementation(libs.byte.buddy.core)

    api(libs.koin.core)
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)
    api(libs.netty)
    api(libs.guava)

    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.netty)
    testImplementation(libs.mockK)
}
