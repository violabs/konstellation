package io.violabs.konstellation.dsl.schema

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import io.violabs.konstellation.dsl.builder.kotlinPoet
import io.violabs.konstellation.dsl.builder.kpListOf
import io.violabs.konstellation.dsl.builder.kpMutableListOf

/**
 * Schema for a property that represents a list of items in the DSL.
 * This is used when the property is expected to hold a collection of items.
 *
 * @param propName The name of the property.
 * @param collectionType The type of elements in the list.
 * @param nullableAssignment Whether the property can be null.
 * @param withVararg Whether to generate a vararg function (default: true).
 * @param withProvider Whether to generate a provider function (default: true).
 */
class ListPropSchema(
    override val propName: String,
    val collectionType: TypeName = STRING,
    override val nullableAssignment: Boolean = true,
    val withVararg: Boolean = true,
    val withProvider: Boolean = true,
) : DslPropSchema {
    override val propTypeName: TypeName = kpListOf(collectionType, nullable = true)
    override val iterableType: DslPropSchema.IterableType = DslPropSchema.IterableType.COLLECTION

    override val verifyNotNull: Boolean = false
    override val verifyNotEmpty: Boolean = true

    override fun toPropertySpec(): PropertySpec = kotlinPoet {
        property {
            protected()
            variable()
            name = propName
            type(propTypeName)

            initNullValue()
        }
    }

    override fun accessors(): List<FunSpec> = kotlinPoet {
        functions {
            // Vararg function: names(vararg name: String)
            if (withVararg) {
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

            // Provider function: names(block: MutableList<String>.() -> Unit)
            if (withProvider) {
                add {
                    funName = functionName
                    param {
                        name = "block"
                        lambdaType {
                            receiver = kpMutableListOf(collectionType, nullable = false)
                        }
                    }
                    statements {
                        addLine("this.%N = mutableListOf<%T>().apply(block).toList()", propName, collectionType)
                    }
                }
            }
        }
    }
}
