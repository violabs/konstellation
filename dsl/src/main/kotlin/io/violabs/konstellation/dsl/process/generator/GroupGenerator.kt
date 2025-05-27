package io.violabs.konstellation.dsl.process.generator

import com.google.devtools.ksp.symbol.KSValueArgument
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import io.violabs.konstellation.metaDsl.annotation.GeneratedDsl
import io.violabs.konstellation.dsl.builder.AnnotationDecorator
import io.violabs.konstellation.dsl.builder.KPTypeSpecBuilder
import io.violabs.konstellation.dsl.domain.DomainConfig
import io.violabs.konstellation.dsl.utils.VLoggable
import kotlin.reflect.KProperty1

/**
 * Abstract class for generating DSL groups.
 * @param T The type of the DSL group.
 * @param config The configuration for the group generator.
 * @param annotationDecorator The decorator for handling annotations in the DSL group.
 */
abstract class GroupGenerator<T>(
    val config: Config<T>,
    val annotationDecorator: AnnotationDecorator
) : VLoggable {
    init {
        logger.enableDebug()
    }

    /**
     * Configuration class for the group generator.
     * This class holds the necessary information to generate the DSL group.
     * @param namespace The namespace for the DSL group.
     * @property property The property of the DSL group that will be used to identify the group.
     * @property templates The templates used for generating the DSL group.
     * @property identifierPredicate A predicate function to identify if the annotation argument matches the group.
     * @property propertyTypeAssigner A function to assign the type variable for the DSL group property.
     * @property builtTypeAssigner A function to assign the type variable for the built DSL group.
     */
    class Config<T>(
        val namespace: Namespace,
        val property: KProperty1<GeneratedDsl, T>,
        val templates: Templates,
        val identifierPredicate: (annotationArg: KSValueArgument) -> Boolean,
        val propertyTypeAssigner: (typeVariable: TypeName?, domainClassName: ClassName) -> TypeName,
        val builtTypeAssigner: (typeVariable: TypeName?, domainClassName: ClassName) -> TypeName,
    )

    /**
     * Namespace class for the group generator.
     * This class holds the namespace information for the DSL group.
     *
     * @property checkName The name used to check if the group is applicable.
     * @property typeName The name of the type for the DSL group.
     * @property typeVariable An optional type variable for the DSL group.
     */
    class Namespace(
        val checkName: String,
        val typeName: String,
        val typeVariable: String? = null
    )

    /**
     * Templates class for the group generator.
     * This class holds the templates used for generating the DSL group.
     *
     * @property prop The property template for the DSL group.
     * @property itemsReturn The return statement for the items function in the DSL group.
     * @property builderAdd The statement for adding an item to the DSL group builder.
     */
    class Templates(
        val prop: String,
        val itemsReturn: String,
        val builderAdd: String
    )

    /**
     * Checks if the given domain configuration is a group based on the provided annotation arguments.
     * This method will look for the `GeneratedDsl` annotation and check if the property matches the group identifier.
     *
     * @param domainConfig The configuration of the domain to check.
     * @return True if the domain is a group, false otherwise.
     */
    protected fun isGroup(domainConfig: DomainConfig): Boolean {
        val isGroup = domainConfig
            .domain
            .annotations
            .filter { it.shortName.asString() == GeneratedDsl::class.simpleName.toString() }
            .flatMap { it.arguments }
            .filter { it.name?.asString() == config.property.name }
            .onEach { logger.debug("found arg: ${it.name?.asString()} - ${it.value}", tier = 1) }
            .any(config.identifierPredicate)
        logger.debug("[DECISION] ${config.namespace.checkName}: $isGroup", tier = 1)

        val typeName = config.namespace.typeName

        logger.debug("$typeName domain", tier = 1, branch = true)
        return isGroup
    }

    /**
     * Generates the DSL group using the provided builder and domain configuration.
     * This method will create a nested type with properties and functions based on the group configuration.
     *
     * @param builder The KotlinPoet builder to generate the DSL group.
     * @param domainConfig The configuration of the domain for which the DSL group is generated.
     */
    fun generate(builder: KPTypeSpecBuilder, domainConfig: DomainConfig) = with(builder) {
        val isGroup = isGroup(domainConfig)

        if (!isGroup) return@with

        val domainClassName = domainConfig.domainClassName

        nested {
            val typeVariable = config.namespace.typeVariable?.let { TypeVariableName(it) }
            addType {
                name = config.namespace.typeName
                typeVariable?.let {
                    typeVariables(it)
                }
                annotations {
                    annotationDecorator
                        .createDslMarkerIfAvailable(domainConfig.builderConfig.dslMarkerClass)
                        ?.also { annotation(it) }
                }
                properties {
                    add {
                        private()
                        name = "items"
                        type(config.propertyTypeAssigner(typeVariable, domainClassName))
                        initializer = config.templates.prop
                    }
                }
                functions {
                    add {
                        funName = "items"
                        returns = config.builtTypeAssigner(typeVariable, domainClassName)
                        statements {
                            addLine(config.templates.itemsReturn)
                        }
                    }

                    add {
                        funName = domainClassName.simpleName.replaceFirstChar { it.lowercase() }

                        typeVariable?.let {
                            param {
                                name = "key"
                                type(it, nullable = false)
                            }
                        }

                        param {
                            lambdaType {
                                receiver = domainConfig.builderClassName
                            }
                        }
                        statements {
                            addLine(config.templates.builderAdd, domainConfig.builderClassName)
                        }
                    }
                }
            }
        }
    }
}

