package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.TypeName

interface TypedSpec : KotlinPoetSpec {
    var type: TypeName?

    fun lambdaType(block: KPLambdaTypeNameBuilder.() -> Unit) {
        typeCheck()
        if (name == null) name = "block"
        type = KPLambdaTypeNameBuilder().apply(block).build()
    }

    fun booleanType() {
        typeCheck()
        if (name == null) name = "on"
        type = BOOLEAN
    }

    fun type(typeName: TypeName) {
        typeCheck()
        type = typeName
    }

    fun type(typeName: TypeName, nullable: Boolean) {
        typeCheck()
        type = typeName.copy(nullable = nullable)
    }


    private fun typeCheck() {
        if (type != null) throw IllegalArgumentException("type already set: $type")
    }
}