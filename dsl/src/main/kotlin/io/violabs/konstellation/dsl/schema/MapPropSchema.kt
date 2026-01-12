package io.violabs.konstellation.dsl.schema

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import io.violabs.konstellation.dsl.builder.kotlinPoet
import io.violabs.konstellation.dsl.builder.kpMapOf
import io.violabs.konstellation.dsl.builder.kpMutableMapOf

/**
 * Schema for a property that represents a map of items in the DSL.
 *
 * @param propName The name of the property.
 * @param mapKeyType The type of keys in the map.
 * @param mapValueType The type of values in the map.
 * @param nullableAssignment Whether the property can be null.
 * @param withVararg Whether to generate a vararg function (default: true).
 * @param withProvider Whether to generate a provider function (default: true).
 */
class MapPropSchema(
    override val propName: String,
    val mapKeyType: TypeName = STRING,
    val mapValueType: TypeName = STRING,
    override val nullableAssignment: Boolean = true,
    val withVararg: Boolean = true,
    val withProvider: Boolean = true,
) : DslPropSchema {
    override val propTypeName: TypeName = kpMapOf(mapKeyType, mapValueType, nullable = true)
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
        val pairType = pairTypeOf(mapKeyType, mapValueType, nullable = false)

        functions {
            // Vararg function: areaCodes(vararg items: Pair<String, String>)
            if (withVararg) {
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

            // Provider function: areaCodes(block: MutableMap<String, String>.() -> Unit)
            if (withProvider) {
                add {
                    funName = functionName
                    param {
                        name = "block"
                        lambdaType {
                            receiver = kpMutableMapOf(mapKeyType, mapValueType, nullable = false)
                        }
                    }
                    statements {
                        addLine(
                            "this.%N = mutableMapOf<%T, %T>().apply(block).toMap()",
                            propName,
                            mapKeyType,
                            mapValueType
                        )
                    }
                }
            }
        }
    }
}
