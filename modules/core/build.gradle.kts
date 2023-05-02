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
}