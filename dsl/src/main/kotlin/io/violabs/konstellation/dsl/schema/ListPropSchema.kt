package io.violabs.konstellation.dsl.schema

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import io.violabs.konstellation.dsl.builder.kotlinPoet
import io.violabs.konstellation.dsl.builder.kpListOf

class ListPropSchema(
    override val propName: String,
    val collectionType: TypeName = STRING,
    override val nullableAssignment: Boolean = true,
) : DslPropSchema {
    override val propTypeName: TypeName = kpListOf(collectionType, nullable = true)
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

    // Example for a list setter (could be more sophisticated, e.g., vararg)
    override fun accessors(): List<FunSpec> = kotlinPoet {
        functions {
            add {
                funName = functionName
                varargParam {
                    type(collectionType, nullable = false)
                }
                statements {
                    addLine("this.%N = items.toList()", propName)
                }
            }
        }
    }
}