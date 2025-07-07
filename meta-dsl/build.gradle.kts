import io.violabs.plugins.open.secrets.getPropertyOrEnv
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URI

plugins {
    id("io.gitlab.arturbosch.detekt")
    `java-library`
    `maven-publish`
    id("io.violabs.plugins.open.secrets.loader")
    id("io.violabs.plugins.open.publishing.maven-generated-artifacts")
    id("io.violabs.plugins.open.publishing.digital-ocean-spaces")
}

group = "io.violabs.konstellation"
version = "0.0.1"

repositories {
    mavenCentral()
}

tasks.jar {
    archiveBaseName.set("meta-dsl")
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
    publishedVersion = version.toString()
}

mavenGeneratedArtifacts {
    publicationName = "digitalOceanSpaces"
    name = "Konstellation DSL Builder Metadata"
    description = """
            Classes for use alongside the Konstellation DSL Builder.
        """
    websiteUrl = "https://github.com/violabs/konstellation/tree/main/meta-dsl"

    licenses {
        license {
            name = "Apache License, Version 2.0"
            url = "https://www.apache.org/licenses/LICENSE-2.0"
        }
    }

    developers {
        developer {
            id = "violabs"
            name = "Violabs Team"
            email = "support@violabs.io"
            organization = "Violabs Software"
        }
    }

    scm {
        connection = "https://github.com/violabs/konstellation.git"
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}