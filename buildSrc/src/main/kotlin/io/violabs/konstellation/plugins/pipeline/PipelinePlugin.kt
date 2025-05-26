package io.violabs.konstellation.plugins.pipeline

import io.violabs.konstellation.plugins.pipeline.tasks.DetectChangeModulesTask
import io.violabs.konstellation.plugins.pipeline.tasks.PrintModulesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class PipelinePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("pipeline", PipelineExtension::class.java)

        target.tasks.register<PrintModulesTask>("printModules") {
            group = "pipeline"
            hasSrc = extension.hasSrc
        }

        target.tasks.register<DetectChangeModulesTask>("detectChangedModules") {
            group = "pipeline"
        }
    }
}