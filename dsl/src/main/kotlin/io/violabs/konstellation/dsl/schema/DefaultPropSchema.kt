package io.violabs.konstellation.dsl.schema

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import io.violabs.konstellation.dsl.process.propSchema.PropertySchemaFactoryAdapter

/**
 * Basic DSL parameter used when no specialized type matches.
 */
class DefaultPropSchema(
    override val propName: String,
    actualPropTypeName: TypeName,
    override val nullableAssignment: Boolean = true,
) : DslPropSchema {
    override val propTypeName: TypeName = actualPropTypeName.copy(nullable = nullableAssignment)

    // Default parameters are public so generated builders can reference them
    override val accessModifier: KModifier = KModifier.PUBLIC

    constructor(adapter: PropertySchemaFactoryAdapter) : this(
        adapter.propName,
        adapter.actualPropTypeName,
        adapter.actualPropTypeName.isNullable
    )
}