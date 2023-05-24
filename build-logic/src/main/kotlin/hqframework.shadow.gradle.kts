plugins {
    java
    id("com.github.johnrengelman.shadow")
}

tasks.shadowJar {
    archiveBaseName.set(project.rootProject.name)
    destinationDirectory.set(file(rootProject.projectDir.path + "/build_outputs"))
}