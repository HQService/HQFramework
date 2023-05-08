plugins {
    id("hqframework.shared")
    id("hqframework.publish")
    kotlin("kapt")
}

dependencies {
    compileOnly(libs.spigot.api)
    api(libs.kotlinx.coroutines.core)
    kapt(libs.auto.service)
    implementation(libs.auto.service.annotations)
    api(project(":modules:core"))

    testImplementation(libs.mockBukkit)
    testImplementation(libs.mockK)
    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(project(":modules:core"))
    testImplementation(project(":modules:test"))
}