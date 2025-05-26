package io.violabs.konstellation.dsl.process.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo
import io.violabs.konstellation.metaDsl.annotation.GeneratedDsl
import io.violabs.konstellation.dsl.builder.AnnotationDecorator
import io.violabs.konstellation.dsl.builder.kotlinPoet
import io.violabs.konstellation.dsl.domain.BuilderConfig
import io.violabs.konstellation.dsl.domain.DomainConfig
import io.violabs.konstellation.dsl.process.DslFileWriter
import io.violabs.konstellation.dsl.process.propSchema.DefaultPropertySchemaService
import io.violabs.konstellation.dsl.schema.DslPropSchema
import io.violabs.konstellation.dsl.utils.VLoggable
import io.violabs.konstellation.dsl.utils.isGroupDsl
import io.violabs.konstellation.dsl.utils.mapGroupType

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

        val schemas: List<DslPropSchema> = parameterService.getParamsFromDomain(domainConfig)

        val builderContent = generateBuilderFileContent(domainConfig, schemas)

        // Check if any validation functions are needed.
        // This logic assumes propertyValueReturn() generates the vRequireNotNull etc. calls.
        val hasRequireNotNull = schemas.any { param -> !param.nullableAssignment && param.verifyNotNull }
        val hasCollectionRequireNotEmpty = schemas.any { param ->
            !param.nullableAssignment && param.verifyNotEmpty && param.isCollection()
        }
        val hasMapRequireNotEmpty = schemas.any { param ->
            !param.nullableAssignment && param.verifyNotEmpty && param.isMap()
        }
        logger.debug("requiresNotNull: $hasRequireNotNull", tier = 1, branch = true)
        logger.debug("requireCollectionNotEmpty: $hasCollectionRequireNotEmpty", tier = 1, branch = true)
        logger.debug("requireMapNotEmpty: $hasMapRequireNotEmpty", tier = 1, branch = true)

        val builderScopeTypeAlias: String = domainConfig.builderName

        val typeAliasNames = mutableListOf(builderScopeTypeAlias)

        val hasGroup = domainConfig.domain.isGroupDsl()
        val hasMapGroup = domainConfig.domain.mapGroupType() in GeneratedDsl.MapGroupType.ACTIVE_TYPES

        if (hasGroup) typeAliasNames.add("${builderScopeTypeAlias}.Group")
        if (hasMapGroup) typeAliasNames.add("${builderScopeTypeAlias}.MapGroup")

        val typeAliases = typeAliasNames
            .onEach {
                logger.debug("typeAlias added: $it", tier = 1, branch = true)
            }
            .map { aliasBaseClassName ->
                kotlinPoet {
                    param {
                        name = aliasBaseClassName
                            .replace(".", "")
                            .let { "${it}Scope" }
                        lambdaType {
                            val hasMap = aliasBaseClassName.contains("MapGroup")
                            val originalClassName = ClassName(domainConfig.packageName, aliasBaseClassName)
                            receiver = if (hasMap)
                                originalClassName.parameterizedBy(TypeVariableName("K"))
                            else
                                originalClassName
                        }
                    }
                }
            }
            .map {
                val hasMap = it.type.toString().contains("MapGroup")
                kotlinPoet {
                    typeAlias {
                        name = it.name
                        type = it.type
                        if (hasMap) typeVariables(TypeVariableName("K"))
                    }
                }
            }

        val fileSpec = kotlinPoet {
            file {
                addImportIf(hasRequireNotNull, "io.violabs.konstellation.metaDsl", "vRequireNotNull")
                addImportIf(hasCollectionRequireNotEmpty, "io.violabs.konstellation.metaDsl", "vRequireCollectionNotEmpty")
                addImportIf(hasMapRequireNotEmpty, "io.violabs.konstellation.metaDsl", "vRequireMapNotEmpty")
                className = domainConfig.fileClassName
                typeAliases(*typeAliases.toTypedArray())
                types(builderContent)
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