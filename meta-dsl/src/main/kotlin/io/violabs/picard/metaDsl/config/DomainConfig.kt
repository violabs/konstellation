package io.violabs.picard.metaDsl.config

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName

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