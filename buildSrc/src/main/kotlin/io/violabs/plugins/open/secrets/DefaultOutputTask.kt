package io.violabs.plugins.open.secrets
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import java.time.Instant

private const val BASE_LOG_TEMPLATE = """
{
    "task": "%s",
    "completionType": "%s",
    "project": "%s",
    "version": "%s",
    "timestamp": "%s",
    "message": "%s"
}
"""

enum class TaskCompletionType {
    SUCCESS,
    ERROR
}

/**
 * Default task for outputting task details to a file.
 */
abstract class DefaultOutputTask : DefaultTask() {
    @get:Input
    abstract val projectName: Property<String>

    @get:Input
    abstract val projectVersion: Property<String>

    @get:OutputFile
    abstract val debugOutputFile: RegularFileProperty

    protected fun output(task: (LogMessage) -> Unit) {
        val message = LogMessage()
        task(message)
        writeOutputToFile(message.successMessage ?: "No message provided")
    }

    protected fun throwAndLogException(message: String): Nothing {
        writeOutputToFile(message)
        throw IllegalArgumentException(message)
    }

    private fun writeOutputToFile(message: String, completionType: TaskCompletionType = TaskCompletionType.SUCCESS) {
        logger.log(LogLevel.LIFECYCLE, message)

        val outputFile = debugOutputFile.get().asFile
        outputFile.parentFile.mkdirs()

        outputFile.writeText(
            BASE_LOG_TEMPLATE.format(
                this::class.simpleName,
                completionType,
                projectName.get(),
                projectVersion.get(),
                Instant.now().toString(),
                message
            )
        )
    }

    protected class LogMessage(var successMessage: String? = null)
}