package io.violabs.plugins.open.publishing.digitalocean

import org.gradle.api.logging.Logger
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.net.URI

abstract class DigitalOceanSpacesClient(
    val ext: DigitalOceanSpacesExtension,
    protected val logger: Logger
) {
    abstract fun uploadFile(file: File)
}

class DryRunDigitalOceanSpacesClient(
    ext: DigitalOceanSpacesExtension,
    logger: Logger
) : DigitalOceanSpacesClient(ext, logger) {
    override fun uploadFile(file: File) {
        if (!file.exists()) return logger.warn("File ${file.name} does not exist, skipping upload")

        val key = "${ext.artifactPath ?: ""}/${file.name}"
        logger.lifecycle("Dry run: would upload ${file.name} to ${ext.bucket}/$key")
    }
}

class DefaultDigitalOceanSpacesClient(
    ext: DigitalOceanSpacesExtension,
    logger: Logger
) : DigitalOceanSpacesClient(ext, logger) {
    internal fun s3Client(): S3Client {
        requireNotNull(ext.accessKey) { "accessKey is required" }
        requireNotNull(ext.secretKey) { "secretKey is required" }

        val credentials = AwsBasicCredentials.create(ext.accessKey, ext.secretKey)

        return S3Client.builder()
            .endpointOverride(URI.create(ext.endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(Region.of(ext.region))
            .build()
    }

    /**
     * Uploads a file to Digital Ocean Spaces.
     * If the file does not exist, it logs a warning and skips the upload.
     *
     * @param file The file to upload.
     */
    override fun uploadFile(file: File) {
        val client = s3Client()
        try {
            client.use {
                if (!file.exists()) return@use logger.warn("File ${file.name} does not exist, skipping upload")

                val key = "${ext.artifactPath ?: ""}/${file.name}"

                logger.lifecycle("Uploading ${file.name} to ${ext.bucket}/$key")

                val request = PutObjectRequest.builder()
                    .bucket(ext.bucket)
                    .key(key)
                    .build()

                it.putObject(request, file.toPath())
            }
        } catch (e: Exception) {
            logger.error("Failed to upload file ${file.name} to Digital Ocean Spaces", e)
            client.close()
        }
    }
}