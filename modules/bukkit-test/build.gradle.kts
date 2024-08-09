plugins {
    id("hqframework.shared")
}

dependencies {
    apiModule("global", "core")
    apiModule("bukkit", "core")
    implementation(libs.paper.api)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.mockBukkit)
    implementation(libs.mockK)
    implementation(libs.junit.parameterizedTest)
}