
plugins {
    kotlin("plugin.serialization") version "2.0.20"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.0")
    implementation(kotlin("test"))
    implementation("org.junit.jupiter:junit-jupiter-api:5.13.0-M2")
    implementation("io.mockk:mockk:1.13.17")
}

tasks.withType<Test> {
    kover {
        isEnabled = true
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}