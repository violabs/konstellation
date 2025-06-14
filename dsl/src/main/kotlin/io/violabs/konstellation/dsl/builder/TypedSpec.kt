package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.TypeName
import io.violabs.konstellation.dsl.exception.KonstellationException

/**
 * A KotlinPoetSpec that allows setting a type for the DSL element.
 */
interface TypedSpec : KotlinPoetSpec {
    /**
     * The type of the DSL element.
     */
    var type: TypeName?

    /**
     * A convenience function to add a lambda type.
     * Available accessors:
     * - [KPLambdaTypeNameBuilder.receiver]
     * - [KPLambdaTypeNameBuilder.returnType]
     * - [KPLambdaTypeNameBuilder.params]
     * @param block A lambda that configures the [KPLambdaTypeNameBuilder] to create a [LambdaTypeName].
     */
    fun lambdaType(block: KPLambdaTypeNameBuilder.() -> Unit) {
        typeCheck()
        if (name == null) name = "block"
        type = KPLambdaTypeNameBuilder().apply(block).build()
    }

    /**
     * Sets the type to [BOOLEAN].
     * If the name is not set, it defaults to "on".
     */
    fun booleanType() {
        typeCheck()
        if (name == null) name = "on"
        type = BOOLEAN
    }

    /**
     * Sets the type to the specified [TypeName].
     * If the name is not set, it defaults to "value".
     *
     * @param typeName The [TypeName] to set as the type.
     */
    fun type(typeName: TypeName) {
        typeCheck()
        type = typeName
    }

    /**
     * Sets the type to the specified [TypeName] with a nullable option.
     * If the name is not set, it defaults to "value".
     *
     * @param typeName The [TypeName] to set as the type.
     * @param nullable Whether the type should be nullable.
     */
    fun type(typeName: TypeName, nullable: Boolean) {
        typeCheck()
        type = typeName.copy(nullable = nullable)
    }

    /**
     * Checks if the type is already set.
     * @throws [IllegalArgumentException] if the type is already set.
     */
    private fun typeCheck() {
        if (type != null) throw KonstellationException("type already set: $type")
    }
}
