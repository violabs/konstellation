plugins {
    kotlin("jvm") version "2.0.20"
    `kotlin-dsl`
    kotlin("plugin.serialization") version "2.0.20"
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.0.20"))
    }
}

repositories {
    // Add any required repositories
    mavenCentral()
}

dependencies {
    implementation("org.springframework:spring-context:6.1.14")

    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.0-rc2")
    annotationProcessor("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.0-rc2")
}

gradlePlugin {
    plugins {
        create("pipeline") {
            id = "io.violabs.plugins.pipeline"
            implementationClass = "io.violabs.konstellation.plugins.pipeline.PipelinePlugin"
        }
    }
}