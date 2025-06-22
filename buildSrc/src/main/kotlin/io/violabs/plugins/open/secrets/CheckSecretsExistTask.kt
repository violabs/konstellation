package io.violabs.plugins.open.secrets

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class CheckSecretsExistTask : DefaultOutputTask() {
    @get:InputFile
    var secretFilePath: File? = null

    @get:Input
    var amountFound: Int = 0

    /**
     * Validates the secret file path and checks if the file exists.
     * If the file does not exist or no secrets are found, it throws an exception.
     * If the file exists and contains secrets, it logs a success message.
     */
    @TaskAction
    fun processSecretFile() = output {
        if (secretFilePath == null) throwAndLogException("Secret file path is not set")

        if (!secretFilePath!!.exists()) throwAndLogException("Secret file does not exist")

        logger.lifecycle("Checking for secrets in ${secretFilePath!!.absolutePath}")

        if (amountFound == 0) throwAndLogException("No secrets found in secret file")

        it.successMessage = "Found $amountFound secrets in secret file"
    }
}