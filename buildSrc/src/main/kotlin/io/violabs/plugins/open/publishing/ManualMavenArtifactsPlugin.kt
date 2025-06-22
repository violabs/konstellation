package io.violabs.plugins.open.publishing

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

class ManualMavenArtifactsPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        pluginManager.apply("java")
        pluginManager.apply("org.jetbrains.dokka")
        pluginManager.apply("maven-publish")

        val sourceSets = project.extensions.getByType<SourceSetContainer>()

        // 1) Sources JAR
        val sourcesJar = tasks.register<Jar>("sourcesJar") {
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }

        // 2) Dokka Javadoc JAR
        val dokkaJavadocJar = tasks.register<Jar>("dokkaJavadocJar") {
            archiveClassifier.set("javadoc")
            from(tasks.named("dokkaJavadoc"))
        }

        // 3) Dokka HTML/KDoc JAR
        val dokkaHtmlJar = tasks.register<Jar>("dokkaHtmlJar") {
            archiveClassifier.set("kdoc")
            from(tasks.named("dokkaHtml"))
        }

        // Configure publishing
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])
                    artifact(sourcesJar)
                    artifact(dokkaJavadocJar)
                    artifact(dokkaHtmlJar)

                    pom {
                        name.set("Konstellation DSL Builder")
                        description.set(
                            """
                            Konstellation automates your Kotlin-DSL generation with KotlinPoet.
                            """.trimIndent()
                        )
                        url.set("https://github.com/violabs/konstellation")

                        licenses {
                            license {
                                name.set("Apache License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                        developers {
                            developer {
                                id.set("joshstallnick")
                                name.set("Josh Stallnick")
                                organization.set("Violabs Software")
                            }
                        }
                        scm {
                            connection.set("scm:git:git://github.com/violabs/konstellation.git")
                            developerConnection.set("scm:git:ssh://github.com:violabs/konstellation.git")
                            url.set("https://github.com/violabs/konstellation")
                        }
                    }
                }
            }
        }

        // 5) Make a single "assembleMavenArtifacts" umbrella task
        tasks.register("assembleMavenArtifacts") {
            dependsOn("jar", sourcesJar, dokkaJavadocJar, dokkaHtmlJar, "generatePomFileForMavenPublication")
            group = "publishing"
            description = "Builds main, sources, javadoc, kdoc jars and the POM."
        }
    }
}