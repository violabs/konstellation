package io.violabs.plugins.open.secrets

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.kotlin.dsl.create
import java.io.File
import java.util.*

/**
 * A plugin that loads secrets from a file or system properties into the project's extra properties.
 */
open class SecretsLoaderPlugin : DefaultOutputPlugin() {
    /**
     * Applies the plugin to the given project.
     * It creates an extension for configuring the secrets loader and registers a task to check if secrets exist.
     * @param project The Gradle project to apply the plugin to.
     */
    override fun apply(project: Project) {
        val extension = project.extensions.create<SecretsLoaderExtension>("secretsLoader")

        project.afterEvaluate {
            logger.lifecycle("Applying SecretsLoaderPlugin to project: ${name}")
            logger.lifecycle(" | [INFO] secretFile: ${extension.secretFile}")
            logger.lifecycle(" | [INFO] systemProperties: ${extension.systemProperties()}")

            val amountProcessed: Int = rootProject.processSecrets(
                extension.secretFile ?: "secret.properties",
                extension.systemProperties()
            )

            logger.lifecycle(" | [INFO] Processed $amountProcessed secrets into extra properties.")

            logger.lifecycle(" | [INFO] Registering `checkSecretsExist` task")
            tasks.register("checkSecretsExist", CheckSecretsExistTask::class.java) {
                this.group = "verification"
                this.description = "Check if secrets exist in the secret file"
                this.secretFilePath = project.rootProject.file(extension.secretFile ?: "secret.properties")
                this.amountFound = amountProcessed

                defaultOutputFileDetails(project, this, CheckSecretsExistTask::class)
            }
        }
    }

    /**
     * Load secrets from a file or system properties into the project's extra properties.
     * If the file exists, it loads properties from the file.
     * If the file does not exist, it loads system properties into the extra properties.
     * @receiver Project The Gradle project to process secrets for.
     * @param secretPropertiesName The name of the properties file to load secrets from.
     * @param systemProperties A map of keys to system property names to load into extra properties.
     * @return The number of properties loaded into the extra properties.
     */
    fun Project.processSecrets(
        secretPropertiesName: String = "secret.properties",
        systemProperties: Map<Ext.Key, Ext.SysPropName> = emptyMap()
    ): Int {
        val secretPropsFile = this.rootProject.file(secretPropertiesName)
        val ext = this.extensions.extraProperties
        var count = 0
        if (secretPropsFile.exists()) {
            logger.lifecycle(" | [INFO] Loading secrets from file: ${secretPropsFile.absolutePath}")
            count = processSecrets(secretPropsFile, ext)
        } else {
            logger.lifecycle(" | [INFO] Secret file not found: ${secretPropsFile.absolutePath}.")
        }

        logger.lifecycle(" | [INFO] Loading system properties into extra properties: $systemProperties")
        count += processSystemProps(logger, systemProperties, ext)

        return count
    }

    /**
     * Load secrets from a file into the extra properties.
     * @param secretPropsFile The file containing the secrets.
     * @param ext The ExtraPropertiesExtension to load the secrets into.
     * @return The number of properties loaded into the extra properties.
     */
    private fun processSecrets(secretPropsFile: File, ext: ExtraPropertiesExtension): Int =
        secretPropsFile
            .reader()
            .use { Properties().apply { load(it) } }
            .onEach { (name, value) -> ext[name.toString()] = value }
            .count()

    /**
     * Load system properties into the extra properties.
     * @param systemProperties A map of keys to system property names to load into extra properties.
     * @return The number of system properties loaded into the extra properties.
     */
    private fun processSystemProps(
        log: Logger,
        systemProperties: Map<Ext.Key, Ext.SysPropName>,
        ext: ExtraPropertiesExtension
    ): Int =
        systemProperties
            .asSequence()
            .filter { (key, sysPropName) ->
                try {
                    ext[key.value] == null && System.getProperty(sysPropName.value) != null
                } catch (e: Exception) {
                    log.warn(" | [WARN] Error accessing system property '${sysPropName.value}': ${e.message}")
                    false
                }
            }
            .onEach { (key, sysPropName) -> ext[key.value] = System.getProperty(sysPropName.value) }
            .count()
}