package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.*

@PicardDSLMarker
class KPTypeSpecBuilder : DefaultKotlinPoetSpec() {
    private var superInterface: TypeName? = null
    private var typeVariables: MutableList<TypeVariableName> = mutableListOf()
    private var annotationNames: MutableList<ClassName> = mutableListOf()
    private var properties: MutableList<PropertySpec> = mutableListOf()
    private var functions: MutableList<FunSpec> = mutableListOf()
    private var nested: MutableList<TypeSpec> = mutableListOf()
    private var sharedGroup: Group? = null

    fun annotation(packageName: String, annotationSimpleName: String) {
        annotationNames.add(ClassName(packageName, annotationSimpleName))
    }

    fun annotations(block: AnnotationGroup.() -> Unit) {
        this.annotationNames = AnnotationGroup().apply(block).annotationNames
    }

    fun superInterface(superInterface: TypeName) {
        this.superInterface = superInterface
    }

    fun typeVariables(vararg typeVariables: String) {
        this.typeVariables = typeVariables.map { TypeVariableName(it) }.toMutableList()
    }

    fun typeVariables(vararg typeVariables: TypeVariableName) {
        this.typeVariables = typeVariables.toMutableList()
    }

    fun properties(block: KPPropertySpecBuilder.Group.() -> Unit) {
        properties = KPPropertySpecBuilder.Group().apply(block).items
    }

    fun properties(properties: List<PropertySpec>) {
        this.properties = properties.toMutableList()
    }

    fun functions(block: KPFunSpecBuilder.Group.() -> Unit) {
        functions = KPFunSpecBuilder.Group().apply(block).items
    }

    fun nested(block: Group.() -> Unit) {
        val group = sharedGroup ?: Group().also { sharedGroup = it }
        nested = group.apply(block).items
    }

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

    class Group {
        val items: MutableList<TypeSpec> = mutableListOf()

        fun addType(block: KPTypeSpecBuilder.() -> Unit) {
            items.add(KPTypeSpecBuilder().apply(block).build())
        }
    }
}