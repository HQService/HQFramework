plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    compileOnly(libs.spigot.api)
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)
    api(libs.koin.core)
    api(libs.koin.annotations)
    api(project(":modules:global-netty"))
    api(project(":modules:global-yaml"))
    ksp(libs.koin.ksp.compiler)

    testImplementation(libs.mockBukkit)
    testImplementation(libs.koin.core)
    testImplementation(libs.koin.annotations)
    testImplementation(libs.mockK)
    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(kotlin("test"))
    testImplementation(project(":modules:test"))
}