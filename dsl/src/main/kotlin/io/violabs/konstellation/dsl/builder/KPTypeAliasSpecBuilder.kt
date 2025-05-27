package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName

/**
 * A builder for creating Kotlin Poets [TypeAliasSpec].
 * @see [https://square.github.io/kotlinpoet/type-aliases/]
 */
class KPTypeAliasSpecBuilder {
    /**
     * The name of the type alias.
     * This is a required field and must be set before building the [TypeAliasSpec].
     */
    var name: String? = null

    /**
     * The type that the alias refers to.
     */
    var type: TypeName? = null
    private var typeVariables: MutableList<TypeVariableName> = mutableListOf()

    /**
     * Sets the name of the type alias.
     * @param name The name of the type alias.
     */
    fun type(type: TypeName) {
        this.type = type
    }

    /**
     * Adds type variables to the type alias.
     *
     * @param typeVariables A variable number of type variable names to be added to the type alias.
     */
    fun typeVariables(vararg typeVariables: String) {
        this.typeVariables = typeVariables.map { TypeVariableName(it) }.toMutableList()
    }

    /**
     * Adds type variables to the type alias.
     *
     * @param typeVariables A variable number of [TypeVariableName] to be added to the type alias.
     */
    fun typeVariables(vararg typeVariables: TypeVariableName) {
        this.typeVariables = typeVariables.toMutableList()
    }

    /**
     * Builds the [TypeAliasSpec] with the specified name, type, and type variables.
     * @return A [TypeAliasSpec] object representing the type alias.
     */
    fun build(): TypeAliasSpec {
        val spec = TypeAliasSpec
            .builder(
                requireNotNull(name) { "name must be set" },
                requireNotNull(type)
            )

        for (variable in typeVariables) {
            spec.addTypeVariable(variable)
        }

        return spec.build()
    }
}