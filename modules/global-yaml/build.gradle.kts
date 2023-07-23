plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)
    api(libs.configurate.yaml)
    api(libs.snakeyaml)

    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.kotlin.test)
}