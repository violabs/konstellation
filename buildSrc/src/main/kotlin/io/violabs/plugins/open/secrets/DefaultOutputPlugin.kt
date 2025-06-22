package io.violabs.plugins.open.secrets

import org.gradle.api.Plugin
import org.gradle.api.Project
import kotlin.reflect.KClass

/**
 * A plugin that provides a default implementation for output file details
 * for tasks that extend [DefaultOutputTask]
 */
abstract class DefaultOutputPlugin : Plugin<Project> {

    /**
     * Sets the default output file details for a task that extends [DefaultOutputTask].
     * This includes the project name, version, and the path to the output file.
     * @param project The Gradle project to which the task belongs.
     * @param task The task for which the output file details are being set.
     * @param klass The KClass of the task
     */
    fun <T : DefaultOutputTask> defaultOutputFileDetails(
        project: Project,
        task: T,
        klass: KClass<T>
    ) {
        val fullProjectName = if (project.name == project.rootProject.name)
            project.name
        else
            "${project.rootProject.name}:${project.name}"

        val folderName = klass.simpleName?.toKebabCase()
        val fileName = klass.simpleName?.toSnakeCase()

        task.projectName.set(fullProjectName)
        task.projectVersion.set(project.version.toString())
        task.debugOutputFile.set(
            project.layout.buildDirectory.file(
                "violabs/tasks/$folderName/${fileName}_output.json"
            )
        )
    }
    
    private fun  String.toKebabCase(): String {
        return this.replace(Regex("([a-z])([A-Z]+)"), "$1-$2").lowercase()
    }

    private fun String.toSnakeCase(): String {
        return this.replace(Regex("([a-z])([A-Z]+)"), "$1_$2").lowercase()
    }
}