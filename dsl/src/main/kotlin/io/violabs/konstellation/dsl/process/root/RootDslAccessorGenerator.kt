package io.violabs.konstellation.dsl.process.root

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.writeTo
import io.violabs.konstellation.dsl.builder.kotlinPoet
import io.violabs.konstellation.dsl.domain.BuilderConfig
import io.violabs.konstellation.dsl.process.DslFileWriter
import io.violabs.konstellation.dsl.utils.VLoggable

interface RootDslAccessorGenerator : DslFileWriter, VLoggable {
    override fun logId(): String? = RootDslAccessorGenerator::class.simpleName

    fun generate(
        codeGenerator: CodeGenerator,
        domains: List<KSClassDeclaration>,
        builderConfig: BuilderConfig
    )
}

class DefaultRootDslAccessorGenerator(
    private val rootFunctionGenerator: DefaultRootFunctionGenerator = DefaultRootFunctionGenerator(),
) : RootDslAccessorGenerator {
    init {
        logger.enableDebug()
    }

    override fun generate(
        codeGenerator: CodeGenerator,
        domains: List<KSClassDeclaration>,
        builderConfig: BuilderConfig
    ) {
        val functions = domains
            .map { rootFunctionGenerator.generate(it, builderConfig) }
            .toMutableList()

        val fileSpec = kotlinPoet {
            file {
                className = ClassName(builderConfig.rootDslFileClasspath(), "RootDslAccessor")
                functions(functions)
            }
        }

        val containingFiles = domains.mapNotNull { it.containingFile }.toTypedArray()

        val dependencies = Dependencies(aggregating = false, sources = containingFiles)

        fileSpec.writeTo(codeGenerator, dependencies)
        logger.debug("file written: RootDslAccessor", tier = 1)
    }
}