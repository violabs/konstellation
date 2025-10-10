package io.violabs.konstellation.dsl.schema

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import io.violabs.konstellation.dsl.schema.model.DslProp
import io.violabs.konstellation.dsl.schema.model.DslPropRenderer

/**
 * Schema for a property that represents a group of items in the DSL.
 */
@Deprecated("Use model-based DslProp with DslPropRenderer; this adapter remains for compatibility.")
class GroupPropSchema(
    override val propName: String,
    private val builtClassName: ClassName,
    override val nullableAssignment: Boolean = true,
    override val kdoc: String? = null,
) : DslPropSchema {
    private val model = DslProp.GroupProp(
        name = propName,
        builtClassName = builtClassName,
        nullableAssignment = nullableAssignment,
        kdoc = kdoc
    )
    // Backward-compatible constructor signature
    constructor(
        propName: String,
        @Suppress("UNUSED_PARAMETER") originalPropertyType: TypeName,
        builtClassName: ClassName,
        nullableAssignment: Boolean = true,
        kdoc: String? = null
    ) : this(propName, builtClassName, nullableAssignment, kdoc)
    // Represented in the builder as a List of built elements
    override val propTypeName: TypeName = io.violabs.konstellation.dsl.builder.kpListOf(builtClassName, nullable = true)
    override val iterableType: DslPropSchema.IterableType = DslPropSchema.IterableType.COLLECTION

    override fun toPropertySpec() = DslPropRenderer.toPropertySpec(model)
    override fun accessors(): List<FunSpec> = DslPropRenderer.accessors(model)
}
