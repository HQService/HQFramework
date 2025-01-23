plugins {
    id("hqframework.shared")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    apiModule("global", "core")
    apiModule("bukkit", "core")
    apiModule("bukkit", "nms")
    api(project(":nms:V21"))
    api(libs.paper.api)
    api(libs.kotlinx.coroutines.core)
    api(libs.exposed.core)
    api(libs.netty)
    api(libs.koin.core)
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")

    testImplementationModule("bukkit", "test")
    testImplementation(libs.mockBukkit)
    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.kotlinx.coroutines.test)
}