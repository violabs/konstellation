
plugins {
    `java-library`
    `maven-publish`
}

group = "io.violabs.picard"
version = "0.0.1"

tasks.jar {
    archiveBaseName.set("picard-common")
}


publishing {
    publications {
        create<MavenPublication>("local") {
            from(components["java"])
            groupId    = "io.violabs.picard"
            artifactId = "common"
            version    = version
        }
    }
}