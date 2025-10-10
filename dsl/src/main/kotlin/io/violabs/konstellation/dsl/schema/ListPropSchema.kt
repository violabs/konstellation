package io.violabs.konstellation.dsl.schema

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import io.violabs.konstellation.dsl.builder.kpListOf
import io.violabs.konstellation.dsl.schema.model.DslProp
import io.violabs.konstellation.dsl.schema.model.DslPropRenderer

/**
 * Schema for a property that represents a list of items in the DSL.
 * This is used when the property is expected to hold a collection of items.
 */
@Deprecated("Use model-based DslProp with DslPropRenderer; this adapter remains for compatibility.")
class ListPropSchema(
    override val propName: String,
    val collectionType: TypeName = STRING,
    override val nullableAssignment: Boolean = true,
) : DslPropSchema {
    private val model = DslProp.ListProp(
        name = propName,
        elementType = collectionType,
        nullableAssignment = nullableAssignment
    )

    override val propTypeName: TypeName = kpListOf(collectionType, nullable = true)
    override val iterableType: DslPropSchema.IterableType = DslPropSchema.IterableType.COLLECTION

    override val verifyNotNull: Boolean = false
    override val verifyNotEmpty: Boolean = true

    override fun toPropertySpec() = DslPropRenderer.toPropertySpec(model)
    override fun accessors(): List<FunSpec> = DslPropRenderer.accessors(model)
}
