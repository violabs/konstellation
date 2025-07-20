package io.violabs.konstellation.dsl.process.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration

import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.joinToCode
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
import kotlin.reflect.KClass

/** * Interface for generating DSL builders.
 * This interface defines the contract for generating DSL builder files based on domain configurations.
 */
interface BuilderGenerator : DslFileWriter, VLoggable {
    override fun logId(): String? = BuilderGenerator::class.simpleName

    /**
     * Generates the DSL builder files for the given domain.
     *
     * @param codeGenerator The KSP CodeGenerator instance used to write the generated files.
     * @param domain The KSClassDeclaration representing the domain for which the builder is generated.
     * @param builderConfig The configuration for the DSL builder.
     * @param singleEntryTransformByClassName A map of class names to their
     *                                        corresponding KSClassDeclaration for single entry transformations.
     */
    fun generate(
        codeGenerator: CodeGenerator,
        domain: KSClassDeclaration,
        builderConfig: BuilderConfig,
        singleEntryTransformByClassName: Map<String, KSClassDeclaration>,
    )
}

/**
 * Default implementation of [BuilderGenerator].
 * This class provides the default behavior for generating DSL builders.
 * @property parameterService The service used to retrieve property schemas from the domain.
 * @property annotationDecorator The decorator for handling annotations in the DSL builder.
 * @property mapGroupGenerator The generator for map groups in the DSL builder.
 * @property listGroupGenerator The generator for list groups in the DSL builder.
 */
class DefaultBuilderGenerator(
    val parameterService: DefaultPropertySchemaService = DefaultPropertySchemaService(),
    val annotationDecorator: AnnotationDecorator = AnnotationDecorator(),
    val mapGroupGenerator: MapGroupGenerator = MapGroupGenerator(),
    val listGroupGenerator: ListGroupGenerator = ListGroupGenerator(),
) : BuilderGenerator {
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

    /**
     * Generates the DSL builder files for the given domain configuration.
     *
     * @param domainConfig The configuration for the domain, including package name, class names, and builder details.
     * @param codeGenerator The KSP CodeGenerator instance used to write the generated files.
     */
    private fun generateFilesForDsl(
        domainConfig: DomainConfig,
        codeGenerator: CodeGenerator
    ) = debugLog(domainConfig) {
        val schemas: List<DslPropSchema> = parameterService.getParamsFromDomain(domainConfig)

        val builderContent: TypeSpec = generateBuilderFileContent(domainConfig, schemas)

        val builderScopeTypeAlias: String = domainConfig.builderName

        val typeAliasNames: MutableList<String> = mutableListOf(builderScopeTypeAlias)

        val hasGroup = domainConfig.domain.isGroupDsl()
        val hasMapGroup = domainConfig.domain.mapGroupType() in GeneratedDsl.MapGroupType.ACTIVE_TYPES

        if (hasGroup) typeAliasNames.add("${builderScopeTypeAlias}.Group")
        if (hasMapGroup) typeAliasNames.add("${builderScopeTypeAlias}.MapGroup")

        val typeAliases: List<TypeAliasSpec> = generateTypeAliases(typeAliasNames, domainConfig)

        val fileSpec = createFileSpec(schemas, domainConfig, typeAliases, builderContent)

        fileSpec.writeTo(codeGenerator, domainConfig.dependencies)
    }

    private fun debugLog(domainConfig: DomainConfig, runnable: () -> Unit) {
        logger.debug("-- generating builder --", tier = 0)
        logger.debug("+++ DOMAIN: ${domainConfig.domainClassName}  +++")
        logger.debug("package: ${domainConfig.packageName}", tier = 1, branch = true)
        logger.debug("type: ${domainConfig.typeName}", tier = 1, branch = true)
        logger.debug("builder: ${domainConfig.builderName}", tier = 1, branch = true)

        runnable()

        logger.debug("file written: ${domainConfig.fileClassName}", tier = 1)
    }

    /**
     * Generates the content of the DSL builder file.
     *
     * @param domainConfig The configuration for the domain, including package name, class names, and builder details.
     * @param params The list of property schemas to be included in the builder.
     * @return A TypeSpec representing the DSL builder interface.
     */
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

    private fun generateTypeAliases(
        typeAliasNames: List<String>,
        domainConfig: DomainConfig
    ): List<TypeAliasSpec> {
        return typeAliasNames
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
    }

    @Suppress("SpreadOperator")
    private fun createFileSpec(
        schemas: List<DslPropSchema>,
        domainConfig: DomainConfig,
        typeAliases: List<TypeAliasSpec>,
        builderContent: TypeSpec
    ): FileSpec {
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

        val defaultValueImports = schemas
            .mapNotNull { it.defaultValue?.importString() }
            .toSet()

        logger.debug("defaultValueImports: $defaultValueImports", tier = 1, branch = true)

        return kotlinPoet {
            file {
                addImportIf(hasRequireNotNull, "io.violabs.konstellation.metaDsl", "vRequireNotNull")
                addImportIf(
                    hasCollectionRequireNotEmpty, "io.violabs.konstellation.metaDsl", "vRequireCollectionNotEmpty"
                )
                addImportIf(hasMapRequireNotEmpty, "io.violabs.konstellation.metaDsl", "vRequireMapNotEmpty")
//                defaultValueImports.forEach {
//
//                }
                className = domainConfig.fileClassName
                typeAliases(*typeAliases.toTypedArray())
                types(builderContent)
            }
        }
    }
}
