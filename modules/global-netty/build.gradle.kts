plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    apiModule("global", "core")
    apiModule("global", "yaml")
    implementation(libs.byte.buddy.core)
    implementation(libs.byte.buddy.agent)
    compileOnly(libs.bungeecord.api)

    api(libs.koin.core)
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)
    api(libs.netty)
    api(libs.guava)
    api(libs.adventure.text.serializer.legacy)

    testImplementationModule("global", "yaml")
    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.mockBukkit)
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.netty)
}