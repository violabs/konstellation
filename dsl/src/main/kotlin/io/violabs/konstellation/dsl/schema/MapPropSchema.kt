package io.violabs.konstellation.dsl.schema

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import io.violabs.konstellation.dsl.builder.kotlinPoet
import io.violabs.konstellation.dsl.builder.kpMapOf

class MapPropSchema(
    override val propName: String,
    val mapKeyType: TypeName = STRING,
    val mapValueType: TypeName = STRING,
    override val nullableAssignment: Boolean = true,
) : DslPropSchema {
    override val propTypeName: TypeName = kpMapOf(mapKeyType, mapValueType, nullable = true)
    override val iterableType: DslPropSchema.IterableType = DslPropSchema.IterableType.COLLECTION

    override val verifyNotNull: Boolean = false
    override val verifyNotEmpty: Boolean = true

    override fun toPropertySpec(): PropertySpec = kotlinPoet {
        property {
            private()
            variable()
            name = propName
            type(propTypeName)

            initNullValue()
        }
    }

    override fun accessors(): List<FunSpec> = kotlinPoet {
        val pairType = pairTypeOf(mapKeyType, mapValueType, nullable = false)

        functions {
            add {
                funName = functionName
                varargParam {
                    type(pairType)
                }
                statements {
                    addLine("this.%N = items.toMap()", propName)
                }
            }
        }
    }
}