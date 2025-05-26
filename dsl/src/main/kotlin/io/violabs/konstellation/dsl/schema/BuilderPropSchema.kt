package io.violabs.konstellation.dsl.schema

import com.squareup.kotlinpoet.*
import io.violabs.konstellation.dsl.builder.kotlinPoet

class BuilderPropSchema(
    override val propName: String,
    originalPropertyType: TypeName,
    private val nestedBuilderClassName: ClassName,
    override val nullableAssignment: Boolean = true,
    kdoc: String? = null
) : DslPropSchema {
    override val propTypeName: TypeName = originalPropertyType
    private val _kdoc: String? = kdoc

    override fun accessors(): List<FunSpec> = kotlinPoet {
        functions {
            add {
                funName = functionName
                _kdoc?.let { kdoc(it) }
                param {
                    lambdaType {
                        receiver = nestedBuilderClassName
                    }
                }

                statements {
                    addLine("val builder = %T()", nestedBuilderClassName)
                    addLine("builder.block()")
                    addLine("this.%N = builder.build()", propName)
                }
            }
        }
    }
}