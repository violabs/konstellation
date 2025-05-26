plugins {
    `java-library`
    `maven-publish`
}

group = "io.violabs.konstellation"
version = "0.0.1"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(project(":meta-dsl"))
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.squareup:kotlinpoet:2.1.0")
    implementation("com.squareup:kotlinpoet-ksp:2.1.0")
    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.20-1.0.32")
    implementation("com.google.auto.service:auto-service:1.1.1")

    testImplementation(project(":core-test"))
}

tasks.jar {
    archiveBaseName.set("dsl")
}

publishing {
    publications {
        create<MavenPublication>("local") {
            from(components["java"])
            groupId    = "io.violabs.konstellation"
            artifactId = "dsl"
            version    = version
        }
    }
}

kover {
    reports {
        filters {
            excludes {
                annotatedBy("io.violabs.konstellation.common.ExcludeFromCoverage")
            }
        }
    }
}