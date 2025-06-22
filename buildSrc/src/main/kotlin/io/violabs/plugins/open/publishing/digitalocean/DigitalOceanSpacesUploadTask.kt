package io.violabs.plugins.open.publishing.digitalocean

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Task to upload project artifacts to Digital Ocean Spaces.
 * This task uploads the main JAR, sources JAR, Javadoc JAR, and POM file
 * to the specified bucket in Digital Ocean Spaces.
 * It uses the configuration provided in the `DigitalOceanSpacesExtension`.
 */
abstract class DigitalOceanSpacesUploadTask : DefaultTask() {
    @get:Input
    abstract var jarQualifier: String?

    @get:Input
    abstract var digitalOceanSpacesClient: DigitalOceanSpacesClient

    /**
     * Uploads the project's artifacts to Digital Ocean Spaces.
     * This task uploads the main JAR, sources JAR, Javadoc JAR, and POM file
     * to the specified bucket in Digital Ocean Spaces.
     * It uses the configuration provided in the `DigitalOceanSpacesExtension`.
     * * The task requires the following properties to be set in the extension:
     * * - `accessKey`: The access key for Digital Ocean Spaces.
     * * - `secretKey`: The secret key for Digital Ocean Spaces.
     * * - `bucket`: The name of the Digital Ocean Spaces bucket.
     * * - `endpoint`: The endpoint URL for Digital Ocean Spaces (default is "https://nyc3.digitaloceanspaces.com").
     * * - `region`: The region for Digital Ocean Spaces (default is "nyc3").
     * * This task will upload the following files:
     * * - Main JAR file: `libs/<project-name>-<version>.jar`
     * * - Sources JAR file: `libs/<project-name>-<version>-sources.jar` (if it exists)
     * * - Javadoc JAR file: `libs/<project-name>-<version>-javadoc.jar` (if it exists)
     * * - POM file: `publications/maven/pom-default.xml` (if it exists)
     * * If any of these files do not exist, they will be skipped, and a warning will be logged.
     */
    @TaskAction
    fun uploadToSpaces() {
        // Get the build directory
        val buildDir: File = project.layout.buildDirectory.get().asFile

        // Files to upload
        val filesToUpload: MutableList<File> = nullishMutableListOf(
            createJar(buildDir),
            createSourcesJar(buildDir),
            createJavadocJar(buildDir),
            createKdocJar(buildDir),
            createPomFile(buildDir)
        )

        // Upload each file
        filesToUpload.forEach(digitalOceanSpacesClient::uploadFile)
    }

    /**
     * Creates a mutable list of non-null items.
     * This function filters out null values from the provided items and returns a mutable list.
     *
     * @param items The items to be included in the list, which can be nullable.
     * @return A mutable list containing only non-null items.
     */
    private fun <T> nullishMutableListOf(vararg items: T?): MutableList<T> {
        return sequenceOf(*items)
            .filterNotNull()
            .toMutableList()
    }

    /**
     * Creates the main JAR file for the project.
     * The JAR file is named using the project name and version.
     *
     * @param buildDir The build directory where the JAR file will be created.
     * @return The created JAR file.
     */
    private fun createJar(buildDir: File): File {
        return File(buildDir, "libs/${jarQualifier ?: project.name}-${project.version}.jar")
    }

    /**
     * Creates the sources JAR file for the project.
     * The sources JAR file is named using the project name and version.
     *
     * @param buildDir The build directory where the sources JAR file will be created.
     * @return The created sources JAR file, or null if it does not exist.
     */
    private fun createSourcesJar(buildDir: File): File? {
        return File(buildDir, "libs/${jarQualifier ?: project.name}-${project.version}-sources.jar").takeIf { it.exists() }
    }

    /**
     * Creates the Javadoc JAR file for the project.
     * The Javadoc JAR file is named using the project name and version.
     *
     * @param buildDir The build directory where the Javadoc JAR file will be created.
     * @return The created Javadoc JAR file, or null if it does not exist.
     */
    private fun createKdocJar(buildDir: File): File? {
        return File(buildDir, "libs/${jarQualifier ?: project.name}-${project.version}-kdoc.jar").takeIf { it.exists() }
    }

    /**
     * Creates the Javadoc JAR file for the project.
     * The Javadoc JAR file is named using the project name and version.
     *
     * @param buildDir The build directory where the Javadoc JAR file will be created.
     * @return The created Javadoc JAR file, or null if it does not exist.
     */
    private fun createJavadocJar(buildDir: File): File? {
        return File(buildDir, "libs/${jarQualifier ?: project.name}-${project.version}-javadoc.jar").takeIf { it.exists() }
    }

    /**
     * Creates the POM file for the project.
     * This method copies the generated POM file from the publications directory
     * to the libs directory with the proper Maven naming convention.
     *
     * @param buildDir The build directory where the POM file will be created.
     * @return The created POM file with proper naming, or null if the source POM does not exist.
     */
    private fun createPomFile(buildDir: File): File? {
        val sourcePom = File(buildDir, "publications/maven/pom-default.xml")
        if (!sourcePom.exists()) {
            return null
        }

        val targetPom = File(buildDir, "libs/${jarQualifier ?: project.name}-${project.version}.pom")

        // Create libs directory if it doesn't exist
        targetPom.parentFile.mkdirs()

        // Copy the POM file with proper naming
        sourcePom.copyTo(targetPom, overwrite = true)

        return targetPom
    }
}