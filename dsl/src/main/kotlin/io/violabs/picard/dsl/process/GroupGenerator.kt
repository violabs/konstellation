package io.violabs.picard.dsl.process

import com.google.devtools.ksp.symbol.KSValueArgument
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import io.violabs.picard.common.VLoggable
import io.violabs.picard.metaDsl.annotation.GeneratedDsl
import io.violabs.picard.metaDsl.builder.AnnotationDecorator
import io.violabs.picard.metaDsl.builder.KPTypeSpecBuilder
import io.violabs.picard.metaDsl.config.DomainConfig
import kotlin.reflect.KProperty1

abstract class GroupGenerator<T>(
    val config: Config<T>,
    val annotationDecorator: AnnotationDecorator
) : VLoggable {
    init {
        logger.enableDebug()
    }

    class Config<T>(
        val namespace: Namespace,
        val property: KProperty1<GeneratedDsl, T>,
        val templates: Templates,
        val identifierPredicate: (annotationArg: KSValueArgument) -> Boolean,
        val propertyTypeAssigner: (typeVariable: TypeName?, domainClassName: ClassName) -> TypeName,
        val builtTypeAssigner: (typeVariable: TypeName?, domainClassName: ClassName) -> TypeName,
    )

    class Namespace(
        val checkName: String,
        val typeName: String,
        val typeVariable: String? = null
    )

    class Templates(
        val prop: String,
        val itemsReturn: String,
        val builderAdd: String
    )

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

