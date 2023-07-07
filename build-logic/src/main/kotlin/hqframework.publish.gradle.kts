import gradle.kotlin.dsl.accessors._db863da26cb404e0ebee710166f16934.sourceSets

plugins {
    `maven-publish`
}


file(rootProject.gradle.rootProject.projectDir.path + "/credentials.gradle.kts").let {
    if (it.exists()) {
        apply(it.path)
    }
}

dependencies {
    "ksp"("io.insert-koin:koin-ksp-compiler:${getProperty("koinAnnotationsVersion")}")
}

val sourcesJar by tasks.registering(Jar::class) {
    from(sourceSets["main"].allSource)
    archiveClassifier.set("sources")

    dependsOn("${project.path}:kspKotlin")
}

publishing {
    publications {
        create<MavenPublication>("nexus") {
            groupId = project.extra["projectGroup"]!!.toString()
            artifactId = "${project.rootProject.name.lowercase()}-${project.project.name.lowercase()}"
            version = project.extra["projectVersion"]!!.toString()

            from(components["java"])

            artifact(sourcesJar.get()) {
                this.classifier = "sources"
            }

            pom {
                name.set(extra["projectName"]?.toString())
                url.set(extra["proejctUrl"]?.toString())
            }
        }
    }
    repositories {
        maven("https://maven.hqservice.kr/repository/maven-snapshots/") {
            credentials {
                if (extra.has("nexusUsername") && extra.has("nexusPassword")) {
                    this.username = getProperty("nexusUsername")
                    this.password = getProperty("nexusPassword")
                }
            }
        }
    }
}