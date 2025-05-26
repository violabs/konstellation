package io.violabs.konstellation.dsl.schema

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import io.violabs.konstellation.dsl.builder.kotlinPoet

// Assuming GroupParam is similar to BuilderParam or a complex type needing its own builder
class GroupPropSchema(
    override val propName: String,
    originalPropertyType: TypeName,
    private val builtClassName: ClassName,
    override val nullableAssignment: Boolean = true,
    private val kdoc: String? = null
) : DslPropSchema {
    override val propTypeName: TypeName = originalPropertyType
    override val iterableType: DslPropSchema.IterableType = DslPropSchema.IterableType.COLLECTION

    override fun toPropertySpec(): PropertySpec = kotlinPoet {
        val assignmentType = listTypeOf(builtClassName)

        property {
            private()
            name = propName
            type(assignmentType)
            mutable()
            initNullValue()
        }
    }

    override fun accessors(): List<FunSpec> = kotlinPoet {
        val receiverName = builtClassName.nestedClass(
            extensionName = "DslBuilder",
            nestedClassName = "Group"
        )

        functions {
            add {
                funName = functionName
                kdoc?.let { kdoc(it) }
                param {
                    lambdaType {
                        receiver = receiverName
                    }
                }
                statements {
                    addLine("this.%N = $receiverName().apply(block).items()", propName)
                }
            }
        }
    }
}