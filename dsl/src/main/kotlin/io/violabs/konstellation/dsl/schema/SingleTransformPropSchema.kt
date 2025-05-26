package io.violabs.konstellation.dsl.schema

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import io.violabs.konstellation.dsl.builder.kotlinPoet
import io.violabs.konstellation.dsl.process.propSchema.PropertySchemaFactoryAdapter

class SingleTransformPropSchema(
    override val propName: String,
    val inputTypeName: TypeName,
    actualPropTypeName: TypeName,
    val transformTemplate: String? = null,
    override val nullableAssignment: Boolean = true,
) : DslPropSchema {
    override val propTypeName: TypeName = actualPropTypeName.copy(nullable = nullableAssignment)

    constructor(adapter: PropertySchemaFactoryAdapter) : this(
        propName = adapter.propName,
        transformTemplate = adapter.transformTemplate,
        actualPropTypeName = adapter.actualPropTypeName,
        inputTypeName = requireNotNull(adapter.transformType) { "input type name is required" },
        nullableAssignment = adapter.hasNullableAssignment
    )

    override fun accessors(): List<FunSpec> = kotlinPoet {
        functions {
            add {
                funName = functionName
                val param = param {
                    name = propName
                    type(inputTypeName)
                }
                statements {
                    val finalTransformTemplate = transformTemplate ?: "${propTypeName.copy(nullable = false)}(%N)"
                    addLine("this.%N = $finalTransformTemplate", propName, param)
                }
            }
        }
    }
}