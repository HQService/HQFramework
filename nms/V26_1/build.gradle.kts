plugins {
    id("hqframework.shared")
    id("io.papermc.paperweight.userdev")
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    apiModule("global", "core")
    apiModule("bukkit", "core")
    apiModule("bukkit", "nms")

    api(libs.paper.api)
    api(libs.kotlinx.coroutines.core)
    api(libs.exposed.core)
    api(libs.netty)
    api(libs.koin.core)
    paperweight.paperDevBundle("26.1.2.build.+")

    testImplementationModule("bukkit", "test")
    testImplementation(libs.mockBukkit)
    testImplementation(libs.junit.parameterizedTest)
    testImplementation(libs.kotlinx.coroutines.test)
}