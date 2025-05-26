plugins {
    `java-library`
    `maven-publish`
}

group = "io.violabs.konstellation"
version = "0.0.2"

repositories {
    mavenCentral()
}

tasks.jar {
    archiveBaseName.set("meta-dsl")
}

publishing {
    publications {
        create<MavenPublication>("local") {
            from(components["java"])
            groupId    = "io.violabs.konstellation"
            artifactId = "meta-dsl"
            version    = version
        }
    }
}