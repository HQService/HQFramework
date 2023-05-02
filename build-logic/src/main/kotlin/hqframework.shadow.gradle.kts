plugins {
    java
    id("com.github.johnrengelman.shadow")
}

tasks.shadowJar {
    this.archiveBaseName.set(project.rootProject.name)
}