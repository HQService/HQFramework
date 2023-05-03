plugins {
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.extra["projectGroup"]!!.toString()
            artifactId = "${project.rootProject.name.lowercase()}-${project.project.name.lowercase()}"
            version = project.extra["projectVersion"]!!.toString()

            from(components["java"])
        }
    }
}