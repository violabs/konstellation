package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.*

/**
 * A builder for creating Kotlin Poets [TypeSpec].
 */
@PicardDSLMarker
class KPTypeSpecBuilder : DefaultKotlinPoetSpec() {
    private var superInterface: TypeName? = null
    private var typeVariables: MutableList<TypeVariableName> = mutableListOf()
    private var annotationNames: MutableList<ClassName> = mutableListOf()
    private var properties: MutableList<PropertySpec> = mutableListOf()
    private var functions: MutableList<FunSpec> = mutableListOf()
    private var nested: MutableList<TypeSpec> = mutableListOf()
    private var sharedGroup: Group? = null

    /**
     * Add annotation to the list.
     * @param packageName The package name of the annotation.
     * @param annotationSimpleName The simple name of the annotation.
     */
    fun annotation(packageName: String, annotationSimpleName: String) {
        annotationNames.add(ClassName(packageName, annotationSimpleName))
    }

    /**
     * Add annotations as a list.
     * available accessors:
     * - [AnnotationGroup.annotation]
     * - [AnnotationGroup.annotationNames]
     * @param block A lambda that configures the [AnnotationGroup].
     */
    fun annotations(block: AnnotationGroup.() -> Unit) {
        this.annotationNames = AnnotationGroup().apply(block).annotationNames
    }

    /**
     * Add an interface to the type (such as a class)
     * @param superInterface The interface to be added as a super interface.
     */
    fun superInterface(superInterface: TypeName) {
        this.superInterface = superInterface
    }

    /**
     * Add type variables to the list.
     * @param typeVariables The type variables to be added.
     */
    fun typeVariables(vararg typeVariables: String) {
        this.typeVariables = typeVariables.map { TypeVariableName(it) }.toMutableList()
    }

    /**
     * Add type variables to the list.
     * @param typeVariables The type variables to be added.
     */
    fun typeVariables(vararg typeVariables: TypeVariableName) {
        this.typeVariables = typeVariables.toMutableList()
    }

    /**
     * Add properties to the list.
     * Available accessors:
     * - [KPPropertySpecBuilder.Group.items]
     * - [KPPropertySpecBuilder.Group.add]
     * - [KPPropertySpecBuilder.Group.addForEachIn]
     * - [KPPropertySpecBuilder.Group.addForEach]
     * * @param block A lambda that configures the property group [KPPropertySpecBuilder.Group] -> Unit.
     */
    fun properties(block: KPPropertySpecBuilder.Group.() -> Unit) {
        properties = KPPropertySpecBuilder.Group().apply(block).items
    }

    /**
     * Add properties to the list.
     * @param properties The properties to be added.
     */
    fun properties(properties: List<PropertySpec>) {
        this.properties = properties.toMutableList()
    }

    /**
     * Add a function to the list.
     * Available accessors:
     * - [KPFunSpecBuilder.funName]
     * - [KPFunSpecBuilder.returns]
     * - [KPFunSpecBuilder.kdoc]
     * - [KPFunSpecBuilder.annotations]
     * - [KPFunSpecBuilder.override]
     * - [KPFunSpecBuilder.statements]
     *
     * @param block A lambda that configures the function [KPFunSpecBuilder] -> Unit.
     */
    fun functions(block: KPFunSpecBuilder.Group.() -> Unit) {
        functions = KPFunSpecBuilder.Group().apply(block).items
    }

    /**
     * Add nested types to the list.
     * Available accessors:
     * - [KPTypeSpecBuilder.Group.items]
     * - [KPTypeSpecBuilder.Group.addType]
     * * @param block A lambda that configures the nested type group [KPTypeSpecBuilder.Group] -> Unit.
     */
    fun nested(block: Group.() -> Unit) {
        val group = sharedGroup ?: Group().also { sharedGroup = it }
        nested = group.apply(block).items
    }

    /**
     * Builds the [TypeSpec] with the specified name, super interface, type variables, annotations, properties, functions, and nested types.
     * @return A [TypeSpec] object representing the type.
     */
    fun build(): TypeSpec {
        var typeBuilder = TypeSpec
            .classBuilder(requireNotNull(name) { "Type - name must be set" })

        superInterface?.let { typeBuilder.addSuperinterface(it) }

        for (variable in typeVariables) {
            typeBuilder = typeBuilder.addTypeVariable(variable)
        }

        for (annotation in annotationNames) {
            typeBuilder = typeBuilder.addAnnotation(annotation)
        }

        for (property in properties) {
            typeBuilder = typeBuilder.addProperty(property)
        }

        for (function in functions) {
            typeBuilder = typeBuilder.addFunction(function)
        }

        for (nestedType in nested) {
            typeBuilder = typeBuilder.addType(nestedType)
        }

        return typeBuilder.build()
    }

    /**
     * A group of types that can be added to a DSL element.
     */
    class Group {
        val items: MutableList<TypeSpec> = mutableListOf()

        /**
         * Adds a type to the list by applying the provided block to a new [KPTypeSpecBuilder].
         * @param block A lambda that configures the [KPTypeSpecBuilder].
         */
        fun addType(block: KPTypeSpecBuilder.() -> Unit) {
            items.add(KPTypeSpecBuilder().apply(block).build())
        }
    }
}