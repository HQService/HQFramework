plugins {
    java
    id("com.github.johnrengelman.shadow")
}

tasks.shadowJar {
    archiveBaseName.set(project.rootProject.name.lowercase())
    archiveVersion.set("")
    archiveClassifier.set(project.name)
    destinationDirectory.set(file(rootProject.projectDir.path + "/build_outputs"))
}