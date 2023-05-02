plugins {
    id("hqframework.shared")
}

dependencies {
    compileOnly(libs.spigot.api)
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)
    api(libs.koin.core)
    api(libs.koin.core.coroutines)
    api(libs.koin.annotations)
    ksp(libs.koin.ksp.compiler)

    testImplementation(libs.mockBukkit)
    testImplementation(libs.koin.core)
    testImplementation(libs.koin.core.coroutines)
    testImplementation(libs.koin.annotations)
    testImplementation(libs.mockK)
    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(kotlin("test"))
}