package io.violabs.konstellation.plugins.pipeline.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * println required for use in GitHub Actions
 */
open class PrintModulesTask : DefaultTask() {
    @Input
    var hasSrc: Boolean = false

    @TaskAction
    fun print() {
        if (hasSrc) println("src")

        project
            .subprojects
            .map { it.path.removePrefix(":") }
            .forEach { println(it) }
    }
}