package io.violabs.picard.dsl.process

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.joinToCode
import com.squareup.kotlinpoet.ksp.writeTo
import io.violabs.picard.common.VLoggable
import io.violabs.picard.metaDsl.config.BuilderConfig
import io.violabs.picard.metaDsl.config.DomainConfig
import io.violabs.picard.metaDsl.schema.DslPropSchema
import io.violabs.picard.metaDsl.builder.AnnotationDecorator
import io.violabs.picard.metaDsl.builder.kotlinPoet
import io.violabs.picard.metaDsl.process.DefaultPropertySchemaService

interface BuilderGenerator : DslFileWriter, VLoggable {
    override fun logId(): String? = BuilderGenerator::class.simpleName

    fun generate(
        codeGenerator: CodeGenerator,
        domain: KSClassDeclaration,
        builderConfig: BuilderConfig,
        singleEntryTransformByClassName: Map<String, KSClassDeclaration>,
    )
}

class DefaultBuilderGenerator(
    val parameterService: DefaultPropertySchemaService = DefaultPropertySchemaService(),
    val annotationDecorator: AnnotationDecorator = AnnotationDecorator(),
    val mapGroupGenerator: MapGroupGenerator = MapGroupGenerator(),
    val listGroupGenerator: ListGroupGenerator = ListGroupGenerator(),
) : BuilderGenerator {
    init {
        logger.enableDebug()
    }

    override fun generate(
        codeGenerator: CodeGenerator,
        domain: KSClassDeclaration,
        builderConfig: BuilderConfig,
        singleEntryTransformByClassName: Map<String, KSClassDeclaration>,
    ) {
        val domainConfig = DomainConfig(
            builderConfig,
            singleEntryTransformByClassName,
            domain
        )
        generateFilesForDsl(domainConfig, codeGenerator)
    }

    private fun generateFilesForDsl(
        domainConfig: DomainConfig,
        codeGenerator: CodeGenerator
    ) {
        logger.debug("-- generating builder --", tier = 0)
        logger.debug("+++ DOMAIN: ${domainConfig.domainClassName}  +++")
        logger.debug("package: ${domainConfig.packageName}", tier = 1, branch = true)
        logger.debug("type: ${domainConfig.typeName}", tier = 1, branch = true)
        logger.debug("builder: ${domainConfig.builderName}", tier = 1, branch = true)

        val params = parameterService.getParamsFromDomain(domainConfig)

        val fileContent = generateBuilderFileContent(domainConfig, params)

        // Check if any validation functions are needed.
        // This logic assumes propertyValueReturn() generates the vRequireNotNull etc. calls.
        val hasRequireNotNull = params.any { param -> !param.nullableAssignment && param.verifyNotNull }
        val hasRequireNotEmpty = params.any { param -> !param.nullableAssignment && param.verifyNotEmpty }
        logger.debug("requiresNotNull: $hasRequireNotNull", tier = 1, branch = true)
        logger.debug("requiresNotEmpty: $hasRequireNotEmpty", tier = 1, branch = true)

        val fileSpec = kotlinPoet {
            file {
                addImportIf(hasRequireNotNull, "io.violabs.picard.metaDsl", "vRequireNotNull")
                addImportIf(hasRequireNotEmpty, "io.violabs.picard.metaDsl", "vRequireNotEmpty")
                className = domainConfig.fileClassName
                types(fileContent)
            }
        }

        fileSpec.writeTo(codeGenerator, domainConfig.dependencies)
        logger.debug("file written: ${domainConfig.fileClassName}", tier = 1)
    }

    private fun generateBuilderFileContent(
        domainConfig: DomainConfig,
        params: List<DslPropSchema>
    ): TypeSpec = kotlinPoet {
        val domainClassName = domainConfig.domainClassName

        type {
            annotations {
                annotationDecorator
                    .createDslMarkerIfAvailable(domainConfig.builderConfig.dslMarkerClass)
                    ?.also { annotation(it) }
            }
            public()
            name = domainConfig.builderName
            superInterface(domainConfig.parameterizedDslBuilder)
            logger.debug("DSL Builder Interface added", tier = 1, branch = true)
            logger.debug("Properties added", tier = 1)

            properties {
                params.addForEach(DslPropSchema::toPropertySpec)
            }

            functions {
                params.addForEach(DslPropSchema::accessors)

                add {
                    override()
                    funName = "build"
                    returns = domainClassName

                    statements {
                        val constructorParams = params
                            .map { CodeBlock.of("%N = %L", it.propName, it.propertyValueReturn()) }
                        if (constructorParams.isEmpty()) {
                            addLine("return %T()", domainClassName)
                        } else {
                            val argumentsBlock = constructorParams.joinToCode(
                                separator = ",\n    ",
                                prefix = "\n    ",
                                suffix = "\n"
                            )
                            addLine("return %T(%L)", domainClassName, argumentsBlock)
                        }
                    }
                }
            }

            listGroupGenerator.generate(this, domainConfig)
            mapGroupGenerator.generate(this, domainConfig)
        }
    }
}