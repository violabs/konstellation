package io.violabs.plugins.open.publishing.digitalocean

/**
 * Extension for configuring the plugin
 */
open class DigitalOceanSpacesExtension {
    /**
     * The access key for DigitalOcean Spaces
     */
    var accessKey: String? = null

    /**
     * The secret key for DigitalOcean Spaces
     */
    var secretKey: String? = null

    /**
     * The region for DigitalOcean Spaces (default: "nyc3")
     */
    var region: String = "nyc3"

    /**
     * The bucket name in DigitalOcean Spaces
     */
    var bucket: String? = null

    /**
     * The endpoint URL for DigitalOcean Spaces (default: "https://nyc3.digitaloceanspaces.com")
     */
    var endpoint: String = "https://nyc3.digitaloceanspaces.com"

    /**
     * The path within the bucket where artifacts will be uploaded
     */
    var artifactPath: String? = null

    var jarQualifier: String? = null

    /**
     * Whether to perform the actual upload or just a dry run
     */
    var dryRun: Boolean = false

    var continueOnVersionCheckFailure: Boolean = true
}