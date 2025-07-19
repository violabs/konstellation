import java.util.Properties
import kotlin.apply
import kotlin.collections.component1
import kotlin.collections.component2

plugins {
    kotlin("jvm") version "2.1.20"
    id("org.jetbrains.dokka") version "1.9.20" apply false
    id("com.google.devtools.ksp") version "2.1.20-1.0.32" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
    application
    id("io.violabs.plugins.pipeline")

    id("io.violabs.plugins.open.publishing.maven-generated-artifacts") version "0.0.13" apply false
    id("io.violabs.plugins.open.publishing.digital-ocean-spaces") version "0.0.8" apply false
}

group = "io.violabs.konstellation"
version = "0.0.1"


extra["dslVersion"] = "0.0.2"
extra["metaDslVersion"] = "0.0.2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
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

        testImplementation(kotlin("test")) // Kotlin’s own assert functions, optional but handy
        testImplementation("org.junit.jupiter:junit-jupiter-api:6.0.0-M1")
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

val secretPropsFile = project.rootProject.file("secret.properties") // update to your secret file under `buildSrc`
val ext = project.extensions.extraProperties
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply { load(it) }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
    project.logger.log(LogLevel.LIFECYCLE, "Secrets loaded from file: $ext")
}