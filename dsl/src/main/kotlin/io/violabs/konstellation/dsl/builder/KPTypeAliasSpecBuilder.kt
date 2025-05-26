package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName

/**
 * @see [https://square.github.io/kotlinpoet/type-aliases/]
 */
class KPTypeAliasSpecBuilder {
    var name: String? = null
    var type: TypeName? = null
    private var typeVariables: MutableList<TypeVariableName> = mutableListOf()

    fun type(type: TypeName) {
        this.type = type
    }

    fun typeVariables(vararg typeVariables: String) {
        this.typeVariables = typeVariables.map { TypeVariableName(it) }.toMutableList()
    }

    fun typeVariables(vararg typeVariables: TypeVariableName) {
        this.typeVariables = typeVariables.toMutableList()
    }

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