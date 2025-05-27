package io.violabs.konstellation.dsl.domain

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Configuration for a domain in the DSL.
 * This class holds the necessary information to generate the DSL builder and related files.
 *
 * @param builderConfig The configuration for the DSL builder.
 * @param singleEntryTransformByClassName A map of class names to their corresponding KSClassDeclaration for single entry transformations.
 * @param domain The KSClassDeclaration representing the domain.
 * @property dslBuilderPostfix The postfix to be used for the DSL builder class name.
 * @property dslBuildFilePostfix The postfix to be used for the DSL build file class name.
 * @property packageName The package name of the domain.
 * @property typeName The simple name of the domain class.
 * @property domainClassName The ClassName of the domain class.
 * @property builderName The name of the DSL builder class.
 * @property builderClassName The ClassName of the DSL builder class.
 * @property dslBuilderInterface The ClassName of the DSL builder interface.
 * @property parameterizedDslBuilder The parameterized type name for the DSL builder interface with the domain class.
 * @property fileClassName The ClassName for the DSL build file.
 * @property dependencies The dependencies for the generated files, including the domain's source file.
 * @constructor Creates a DomainConfig instance with the specified parameters.
 */
open class DomainConfig(
    val builderConfig: BuilderConfig,
    val singleEntryTransformByClassName: Map<String, KSClassDeclaration>,
    var domain: KSClassDeclaration,
) {
    open val dslBuilderPostfix: String = "DslBuilder"
    open val dslBuildFilePostfix: String = "Dsl"
    val packageName = domain.packageName.asString()
    val typeName = domain.simpleName.asString()
    val domainClassName: ClassName = domain.toClassName()
    val builderName = "${typeName}$dslBuilderPostfix"
    val builderClassName = ClassName(packageName, builderName)
    val dslBuilderInterface = ClassName(builderConfig.dslBuilderClasspath, dslBuilderPostfix)
    val parameterizedDslBuilder = dslBuilderInterface.parameterizedBy(domainClassName)

    open val fileClassName = ClassName(packageName, "${typeName}$dslBuildFilePostfix")
    val dependencies = Dependencies(aggregating = false, sources = listOfNotNull(domain.containingFile).toTypedArray())
}