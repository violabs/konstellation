package io.violabs.plugins.open.publishing.digitalocean

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import java.io.File
import kotlin.jvm.Throws

/**
 * Task to check if a specific version of an artifact already exists in Digital Ocean Spaces.
 * If the version exists, it throws an exception; otherwise, it logs a notice message.
 * This task is intended to be used in a Gradle build script to prevent accidental overwriting
 * of existing versions in Digital Ocean Spaces.
 */
abstract class DigitalOceanSpacesCheckVersionTask : DefaultTask() {
    /**
     * The extension for configuring Digital Ocean Spaces access information.
     * This property is used to retrieve the access key, secret key, bucket name,
     * endpoint, and region for Digital Ocean Spaces.
     * It is expected to be set in the build script using the `digitalOceanSpaces` extension.
     */
    @get:Input
    abstract val extension: Property<DigitalOceanSpacesExtension>

    @get:Input
    abstract var s3Client: S3Client

    /**
     * Checks if the current project version already exists in Digital Ocean Spaces.
     * If the version exists, it throws a [GradleException] with an error message.
     * If the version does not exist, it logs a notice message.
     * This task is intended to be used in a Gradle build script to prevent
     * accidental overwriting of existing versions in Digital Ocean Spaces.
     * This task requires the following properties to be set in the extension:
     * - `accessKey`: The access key for Digital Ocean Spaces.
     * - `secretKey`: The secret key for Digital Ocean Spaces.
     * - `bucket`: The name of the Digital Ocean Spaces bucket.
     * - `endpoint`: The endpoint URL for Digital Ocean Spaces (default is "https://nyc3.digitaloceanspaces.com").
     * - `region`: The region for Digital Ocean Spaces (default is "nyc3").
     * * The task will also append metadata to the GITHUB_OUTPUT file if it is available.
     * This metadata includes the project version, name, and tag.
     * This is useful for GitHub Actions to capture the version information.
     * @throws GradleException if the version already exists in Digital Ocean Spaces.
     * @throws IllegalArgumentException if any of the required access information is missing.
     */
    @TaskAction
    fun checkVersion() {
        val ext = extension.get()
        val bucket = requireNotNull(ext.bucket) { "bucket is required" }

        addMetadataIfOutputFileIsAvailable()

        val key = createSpacesFileKey(ext)

        val request = buildRequest(ext)

        try {
            s3Client.processExistingVersion(bucket, key, request)
        } catch (_: NoSuchKeyException) {
            processNewVersion()
        }
    }

    /**
     * Adds metadata to the GITHUB_OUTPUT file if it is available.
     * The metadata includes the project version, name, and tag.
     * This is useful for GitHub Actions to capture the version information.
     * @param fileName The name of the output file, defaults to the GITHUB_OUTPUT environment variable.
     */
    private fun addMetadataIfOutputFileIsAvailable(fileName: String? = System.getenv("GITHUB_OUTPUT")) {
        if (fileName.isNullOrBlank()) {
            return
        }

        File(fileName).appendText(
            """
                version=${project.version}
                name=${project.name}
                tag=${project.name}-${project.version}
                """.trimIndent() + "\n"
        )
    }

    /**
     * Creates the key for the file in Digital Ocean Spaces.
     * Uses the artifactPath if provided, otherwise defaults to an empty string.
     * The key is structured as:
     * ```
     * artifactPath/projectName-projectVersion.jar
     * ```
     * @param ext The DigitalOceanSpacesExtension containing configuration information.
     * @return The key for the file in Digital Ocean Spaces.
     */
    private fun createSpacesFileKey(ext: DigitalOceanSpacesExtension): String {
        return "${ext.artifactPath ?: ""}/${project.name}-${project.version}.jar"
    }

    /**
     * Builds the request to check if the object exists in Digital Ocean Spaces.
     * @param ext The DigitalOceanSpacesExtension containing configuration information.
     * @return The HeadObjectRequest to check for the object's existence.
     */
    private fun buildRequest(ext: DigitalOceanSpacesExtension): HeadObjectRequest {
        return HeadObjectRequest.builder()
            .bucket(ext.bucket)
            .key(createSpacesFileKey(ext))
            .build()
    }

    /**
     * Attempts a check for an existing version in Digital Ocean Spaces.
     * If the version exists, it throws a [GradleException] with an error message.
     * If it does not exist, it throws [NoSuchKeyException]
     * @param bucket The name of the Digital Ocean Spaces bucket.
     * @param key The key of the object in the bucket.
     * @param request The HeadObjectRequest to check for the object's existence.
     */
    @Throws(exceptionClasses = [GradleException::class, NoSuchKeyException::class])
    private fun S3Client.processExistingVersion(
        bucket: String,
        key: String,
        request: HeadObjectRequest
    ) {
        headObject(request)
        // Version exists - throw error
        val errorMessage = """
                    |::error::Version ${project.version} already exists in Digital Ocean Spaces
                    |Artifact: ${project.name}
                    |Path: $bucket}/$key
                    |Tag: ${project.name}-${project.version}
                    |Please update the version number in your build.gradle.kts file.
                """.trimMargin()

        throw GradleException(errorMessage)
    }

    /**
     * Processes the case where the version does not exist in Digital Ocean Spaces.
     * Logs a notice message with the version, artifact name, and tag.
     * This method is called when the version check passes successfully.
     */
    private fun processNewVersion() {
        // Version doesn't exist
        logger.lifecycle(
            """
                    |::notice::Version check passed
                    |Version: ${project.version}
                    |Artifact: ${project.name}
                    |Tag: ${project.name}-${project.version}
                """.trimMargin()
        )
    }
}