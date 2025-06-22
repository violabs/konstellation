package io.violabs.plugins.open.publishing.digitalocean

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

/**
 * Plugin to manage publishing jars to digital ocean spaces.
 */
class DigitalOceanSpacesPublishPlugin : Plugin<Project> {
    /**
     * Applies the extension and registers tasks for Digital Ocean Spaces publishing.
     * @param project The Gradle project to which this plugin is applied.]
     */
    override fun apply(project: Project) = project.run {
        pluginManager.apply("io.violabs.plugins.open.publishing.manual-maven-artifacts")

        // Create the extension
        val extension = project.extensions.create<DigitalOceanSpacesExtension>("digitalOceanSpacesPublishing")

        project.afterEvaluate {
            project.logger.lifecycle("Applying DigitalOceanSpacesPublishPlugin to project: ${project.name}")
            project.logger.lifecycle(" | [INFO] endpoint: ${extension.endpoint}")
            project.logger.lifecycle(" | [INFO] bucket: ${extension.bucket}")
            project.logger.lifecycle(" | [INFO] region: ${extension.region}")
            project.logger.lifecycle(" | [INFO] artifactPath: ${extension.artifactPath}")
            project.logger.lifecycle(" | [INFO] dryRun: ${extension.dryRun}")
            logger.lifecycle(" | [INFO] Registering `checkDigitalOceanSpacesVersion` task")

            val doSpacesClient = DefaultDigitalOceanSpacesClient(extension, project.logger)
            // Register the version check task
            tasks.register<DigitalOceanSpacesCheckVersionTask>("checkDigitalOceanSpacesVersion") {
                group = "verification"
                description = "Checks if the current version already exists in Digital Ocean Spaces"
                this.extension.set(extension)
                this.s3Client = doSpacesClient.s3Client()
            }

            logger.lifecycle(" | [INFO] Registering `uploadToDigitalOceanSpaces` task")
            if (extension.dryRun) {
                logger.lifecycle(" | [INFO] Dry run mode enabled, uploads will not be performed.")
            }
            // Register the upload task
            tasks.register<DigitalOceanSpacesUploadTask>("uploadToDigitalOceanSpaces") {
                group = "publishing"
                description = "Uploads artifacts to Digital Ocean Spaces"
                this.digitalOceanSpacesClient = if (extension.dryRun) {
                    DryRunDigitalOceanSpacesClient(extension, project.logger)
                } else {
                    doSpacesClient
                }
                jarQualifier = extension.jarQualifier ?: project.name

                // Make sure we run after the build task
                dependsOn("build", "assembleMavenArtifacts")

                // If using the maven-publish plugin, also depend on publish tasks
                plugins.withId("maven-publish") {
                    dependsOn("publishToMavenLocal")
                    dependsOn("generatePomFileForMavenPublication")
                }
            }
        }
    }
}