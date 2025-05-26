package io.violabs.konstellation.plugins.pipeline.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

open class DetectChangeModulesTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {

    @TaskAction
    fun detectChangeModules() {
        println("Detecting changed modules...")
        val changedFiles = getChangedFiles()

        if (changedFiles.isEmpty()) {
            println("""MATRIX=[]""")  // ✅ Always return valid JSON
            return
        }

        // Convert Gradle's ":"-based module names to filesystem paths
        val gradleModules = project.subprojects.map { it.path }

        val allModulePaths = gradleModules.toPaths()

        // Identify affected modules, considering submodules properly
        val changedFileModulePaths = allModulePaths.filterHasChangedFiles(changedFiles)

        val moduleDetails = changedFileModulePaths.toNamespaceJson()

        println("MATRIX=$moduleDetails")
    }

    private fun getChangedFiles(): List<String> {
        return System.getenv("CHANGED_FILES")
            ?.split(" ")
            ?.filter { it.isNotBlank() }
            ?: run { execOperations.getDiff() }
    }

    private fun ExecOperations.getDiff(): List<String> {
        val outputStream = ByteArrayOutputStream()
        this.exec {
            commandLine("git", "diff", "--name-only", "origin/main...HEAD")
            standardOutput = outputStream
        }
        return outputStream.toFilePathList()
    }

    private fun ByteArrayOutputStream.toFilePathList(): List<String> {
        return toString()
            .trim()
            .split("\n")
            .filter { it.isNotBlank() }
    }

    private fun List<String>.toPaths(): Set<String> {
        return map { it.removePrefix(":").replace(":", "/") }
            .sortedByDescending { it.length }
            .toSet()
    }

    private fun Set<String>.filterHasChangedFiles(changedFiles: List<String>): Set<String> {
        return changedFiles.mapNotNull { file ->
            this.find { module ->
                file.startsWith("$module/") || file.startsWith("$module\\")
            } ?: "src".takeIf { file.startsWith(it) }
        }.toSet()
    }

    private fun Set<String>.toNamespaceJson(): String {
        return joinToString(",", "[", "]") {
            val module = it.replace("/", ":")
            val filename = it.replace("/", "-")

            """{"module":":$module","path":"$it","filename":"$filename"}"""
        }
    }
}