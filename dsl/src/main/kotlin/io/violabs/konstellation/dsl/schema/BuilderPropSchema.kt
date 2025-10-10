package io.violabs.konstellation.dsl.schema

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import io.violabs.konstellation.dsl.schema.model.DslProp
import io.violabs.konstellation.dsl.schema.model.DslPropRenderer

/**
 * Schema for a property that uses a builder pattern in the DSL.
 */
@Deprecated("Use model-based DslProp with DslPropRenderer; this adapter remains for compatibility.")
class BuilderPropSchema(
    override val propName: String,
    originalPropertyType: TypeName,
    private val nestedBuilderClassName: ClassName,
    override val nullableAssignment: Boolean = true,
    override val kdoc: String? = null
) : DslPropSchema {
    private val model = DslProp.BuilderProp(
        name = propName,
        originalType = originalPropertyType,
        nestedBuilderClassName = nestedBuilderClassName,
        nullableAssignment = nullableAssignment,
        kdoc = kdoc
    )

    override val propTypeName: TypeName = originalPropertyType

    override fun toPropertySpec() = DslPropRenderer.toPropertySpec(model)
    override fun accessors(): List<FunSpec> = DslPropRenderer.accessors(model)
}
