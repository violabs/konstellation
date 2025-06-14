
plugins {
    kotlin("jvm") version "2.1.20"
    id("org.jetbrains.dokka") version "1.9.20" apply false
    id("com.google.devtools.ksp") version "2.1.20-1.0.32" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
    application
    id("io.violabs.plugins.pipeline")
}

group = "io.violabs"
version = "0.0.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

sharedRepositories()

allprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.dokka")
        plugin("application")
        plugin("org.jetbrains.kotlinx.kover")
    }

    sharedRepositories()

    dependencies {
        implementation(kotlin("stdlib"))
        implementation("org.jetbrains.kotlin:kotlin-reflect")

        implementation("io.github.microutils:kotlin-logging:4.0.0-beta-2")

        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
    }
}

fun Project.sharedRepositories() {
    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://www.jetbrains.com/intellij-repository/releases") }
    }
}

tasks.register("koverMergedReport") {
    group = "verification"
    description = "Generates merged coverage report for all modules"

    dependsOn(subprojects.map { it.tasks.named("koverXmlReport") })
}