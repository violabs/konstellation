package io.violabs.konstellation.dsl.schema

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import io.violabs.konstellation.dsl.domain.DefaultPropertyValue
import io.violabs.konstellation.dsl.schema.model.DslProp
import io.violabs.konstellation.dsl.schema.model.DslPropRenderer

/**
 * Schema for a boolean property in the DSL.
 */
@Deprecated("Use model-based DslProp with DslPropRenderer; this adapter remains for compatibility.")
class BooleanPropSchema(
    override val propName: String,
    override val nullableAssignment: Boolean = true,
    override val defaultValue: DefaultPropertyValue? = null
) : DslPropSchema {
    private val model = DslProp.BooleanProp(
        name = propName,
        nullableAssignment = nullableAssignment,
        defaultValue = defaultValue
    )

    override val propTypeName: TypeName = BOOLEAN.copy(nullable = nullableAssignment)

    override fun toPropertySpec() = DslPropRenderer.toPropertySpec(model)
    override fun accessors(): List<FunSpec> = DslPropRenderer.accessors(model)
}
