package io.violabs.konstellation.dsl.schema

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import io.violabs.konstellation.dsl.domain.DefaultPropertyValue
import io.violabs.konstellation.dsl.process.propSchema.PropertySchemaFactoryAdapter
import io.violabs.konstellation.dsl.schema.model.DslProp
import io.violabs.konstellation.dsl.schema.model.DslPropRenderer

/**
 * Basic DSL parameter used when no specialized type matches.
 */
@Deprecated("Use model-based DslProp with DslPropRenderer; this adapter remains for compatibility.")
class DefaultPropSchema(
    override val propName: String,
    actualPropTypeName: TypeName,
    override val nullableAssignment: Boolean = true,
    override val defaultValue: DefaultPropertyValue? = null
) : DslPropSchema {
    private val model = DslProp.Default(
        name = propName,
        actualType = actualPropTypeName,
        nullableAssignment = nullableAssignment,
        defaultValue = defaultValue,
        accessModifier = KModifier.PUBLIC
    )

    override val propTypeName: TypeName = actualPropTypeName.copy(nullable = nullableAssignment)

    // Default parameters are public so generated builders can reference them
    override val accessModifier: KModifier = KModifier.PUBLIC

    constructor(adapter: PropertySchemaFactoryAdapter) : this(
        adapter.propName,
        adapter.actualPropTypeName,
        adapter.actualPropTypeName.isNullable
    )

    override fun toPropertySpec() = DslPropRenderer.toPropertySpec(model)
    override fun accessors() = DslPropRenderer.accessors(model)
}
