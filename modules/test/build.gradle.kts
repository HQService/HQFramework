plugins {
    id("hqframework.shared")
}

dependencies {
    implementation(libs.spigot.api)
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":modules:core"))

    implementation(libs.mockBukkit)
    implementation(libs.mockK)
    implementation(libs.junit.parameterizedTest)
}