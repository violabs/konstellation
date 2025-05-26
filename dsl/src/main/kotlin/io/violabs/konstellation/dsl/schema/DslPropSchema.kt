package io.violabs.konstellation.dsl.schema

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import io.violabs.konstellation.dsl.builder.kotlinPoet

/**
 * Represents a property in a generated DSL builder.
 */
interface DslPropSchema {
    val propName: String
    val functionName: String get() = propName
    val propTypeName: TypeName // This should be the type of the actual property in the domain object
    val nullableAssignment: Boolean get() = true
    val verifyNotNull: Boolean get() = true
    val verifyNotEmpty: Boolean get() = false
    val iterableType: IterableType? get() = null
    val accessModifier: KModifier get() = KModifier.PRIVATE

    fun isCollection(): Boolean = iterableType == IterableType.COLLECTION
    fun isMap(): Boolean = iterableType == IterableType.MAP

    /**
     * Create the KotlinPoet [PropertySpec] representing this DSL property.
     */
    fun toPropertySpec(): PropertySpec = kotlinPoet {
        property {
            accessModifier(accessModifier)
            variable()
            name = propName
            type(propTypeName.copy(nullable = true))

            initNullValue()
        }
    }

    // Added containingBuilderClassName to allow fluent return types
    /**
     * Generate any accessor functions (such as DSL builder methods) for this parameter.
     */
    fun accessors(): List<FunSpec> {
        return emptyList()
    }

    /**
     * Provide the code snippet used when returning this parameter's value.
     */
    fun propertyValueReturn(): String {
        if (nullableAssignment) return propName

        return if (verifyNotNull) {
            "vRequireNotNull(::$propName)" // Added message for vRequireNotNull
        } else if (verifyNotEmpty && isCollection()) {
            "vRequireCollectionNotEmpty(::$propName)"
        } else if (verifyNotEmpty && isMap()) {
            "vRequireMapNotEmpty(::$propName)"
        } else {
            propName
        }
    }

    enum class IterableType {
        COLLECTION, MAP
    }
}