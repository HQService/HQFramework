plugins {
    id("hqframework.shared")
}

repositories {
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly(libs.spigot.api)
    compileOnly("com.sk89q.worldedit", "worldedit-bukkit", "7.2.13")
    compileOnly("com.sk89q.worldguard", "worldguard-bukkit", "7.0.7")
    api(project(":modules:core"))
    api(libs.kotlinx.coroutines.core)
}