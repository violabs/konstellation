package io.violabs.konstellation.dsl.process.root

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import io.violabs.konstellation.dsl.builder.kotlinPoet
import io.violabs.konstellation.dsl.domain.BuilderConfig

/**
 * Interface for generating the root DSL function.
 */
interface RootFunctionGenerator {
    fun generate(
        domain: KSClassDeclaration,
        builderConfig: BuilderConfig
    ): FunSpec
}

/**
 * Default implementation of [RootFunctionGenerator].
 * This class generates a root function for the DSL that initializes a builder for the given domain.
 */
class DefaultRootFunctionGenerator : RootFunctionGenerator {
    override fun generate(
        domain: KSClassDeclaration,
        builderConfig: BuilderConfig
    ): FunSpec = kotlinPoet {
        function {
            val domainClassName = domain.toClassName()
            val domainBuilderClassName =
                ClassName(domainClassName.packageName, "${domainClassName.simpleName}DslBuilder")
            funName = domain.simpleName.asString().replaceFirstChar { it.lowercase() }
            // todo: add docs

            param {
                lambdaType {
                    receiver = domainBuilderClassName
                }
            }

            returns = domainClassName

            statements {
                addLine("val builder = %T()", domainBuilderClassName)
                addLine("builder.block()")
                addLine("return builder.build()")
            }
        }
    }
}
