plugins {
    id("hqframework.shared")
    id("hqframework.publish")
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.netty)
    api(libs.guava)

    testImplementation(libs.junit.parameterizedTest)
}