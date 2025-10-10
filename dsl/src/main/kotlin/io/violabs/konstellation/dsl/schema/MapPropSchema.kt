package io.violabs.konstellation.dsl.schema

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import io.violabs.konstellation.dsl.builder.kpMapOf
import io.violabs.konstellation.dsl.schema.model.DslProp
import io.violabs.konstellation.dsl.schema.model.DslPropRenderer

/**
 * Schema for a property that represents a map of items in the DSL.
 */
@Deprecated("Use model-based DslProp with DslPropRenderer; this adapter remains for compatibility.")
class MapPropSchema(
    override val propName: String,
    val mapKeyType: TypeName = STRING,
    val mapValueType: TypeName = STRING,
    override val nullableAssignment: Boolean = true,
) : DslPropSchema {
    private val model = DslProp.MapProp(
        name = propName,
        keyType = mapKeyType,
        valueType = mapValueType,
        nullableAssignment = nullableAssignment
    )

    override val propTypeName: TypeName = kpMapOf(mapKeyType, mapValueType, nullable = true)
    override val iterableType: DslPropSchema.IterableType = DslPropSchema.IterableType.COLLECTION

    override val verifyNotNull: Boolean = false
    override val verifyNotEmpty: Boolean = true

    override fun toPropertySpec() = DslPropRenderer.toPropertySpec(model)
    override fun accessors(): List<FunSpec> = DslPropRenderer.accessors(model)
}
