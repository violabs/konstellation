package io.violabs.konstellation.dsl.schema

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import io.violabs.konstellation.dsl.builder.kotlinPoet
import io.violabs.konstellation.dsl.domain.DefaultPropertyValue

/**
 * Schema for a boolean property in the DSL.
 */
class BooleanPropSchema(
    override val propName: String,
    override val nullableAssignment: Boolean = true,
    override val defaultValue: DefaultPropertyValue? = null
) : DslPropSchema {
    override val propTypeName: TypeName = BOOLEAN.copy(nullable = nullableAssignment) // Correctly use constructor arg

    override fun accessors(): List<FunSpec> = kotlinPoet {
        functions {
            add {
                funName = propName
                val param = param {
                    booleanType()
                    defaultValue(defaultValue?.rawValue?.toBoolean() ?: true)
                }
                statements {
                    addLine("this.%N = %N", propName, param)
                }
            }
        }
    }
}
