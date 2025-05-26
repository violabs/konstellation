package io.violabs.konstellation.dsl.schema

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.violabs.konstellation.dsl.builder.kotlinPoet
import io.violabs.konstellation.dsl.builder.kpMapOf

class MapGroupPropSchema(
    override val propName: String,
    val mapKeyType: TypeName = STRING,
    val mapValueType: TypeName,
    override val nullableAssignment: Boolean = true,
    private val kdoc: String? = null
) : DslPropSchema {
    override val propTypeName: TypeName = kpMapOf(mapKeyType, mapValueType, nullable = true)
    override val iterableType: DslPropSchema.IterableType = DslPropSchema.IterableType.MAP

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
        val valueClassName = mapValueType.copy(nullable = false) as ClassName
        val mapGroupClass = ClassName(
            valueClassName.packageName,
            valueClassName.simpleName + "DslBuilder",
            "MapGroup"
        ).parameterizedBy(mapKeyType.copy(nullable = false))

        functions {
            add {
                funName = functionName
                kdoc?.let { kdoc(it) }
                param {
                    lambdaType {
                        receiver = mapGroupClass
                    }
                }

                statements {
                    addLine("this.%N = %T().apply(block).items().toMap()", propName, mapGroupClass)
                }
            }
        }
    }
}