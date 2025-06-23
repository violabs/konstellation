import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.violabs.plugins.open.secrets.getPropertyOrEnv
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URI

plugins {
    id("io.gitlab.arturbosch.detekt")
    `java-library`
    `maven-publish`
    id("io.violabs.plugins.open.secrets.loader")
    id("io.violabs.plugins.open.publishing.digital-ocean-spaces")
    id("io.violabs.plugins.open.publishing.maven-generated-artifacts") version "0.0.1"
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
//    dependsOn(subprojects.map { it.tasks.named("classes") })
//
//    // Pull in each subproject’s compiled classes & resources
//    from(subprojects.map { proj ->
//        proj.extensions.getByType<SourceSetContainer>()["main"].output
//    })
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


detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
//    config.setFrom("$projectDir/config/detekt.yml") // point to your custom config defining rules to run, overwriting default behavior
//    baseline = file("$projectDir/config/baseline.xml") // a way of suppressing issues before introducing detekt
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true) // observe findings in your browser with structure and code snippets
        xml.required.set(true) // checkstyle like format mainly for integrations like Jenkins
        txt.required.set(true) // similar to the console output, contains issue signature to manually edit baseline files
        sarif.required.set(true) // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with GitHub Code Scanning
        md.required.set(true) // simple Markdown format
    }
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = JavaVersion.VERSION_21.majorVersion
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = JavaVersion.VERSION_21.majorVersion
}

tasks.named<DokkaTask>("dokkaJavadoc") {
    dokkaSourceSets {
        named("main") {
            includeNonPublic.set(true)
            skipDeprecated.set(true)
            jdkVersion.set(17)
            sourceLink {
                val uri: URI = URI.create("https://github.com/violabs/konstellation")
                this.remoteUrl.set(uri.toURL())
                this.remoteLineSuffix.set("#L")
                this.localDirectory.set(project.projectDir)
            }
        }
    }
}

digitalOceanSpacesPublishing {
    bucket = "open-reliquary"
    accessKey = project.getPropertyOrEnv("spaces.key", "DO_SPACES_API_KEY")
    secretKey = project.getPropertyOrEnv("spaces.secret", "DO_SPACES_SECRET")
    artifactPath = "io/violabs/konstellation/dsl/$version"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17) // Specify your desired Java version here
    }
}