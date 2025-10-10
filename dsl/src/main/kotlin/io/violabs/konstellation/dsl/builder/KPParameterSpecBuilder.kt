package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName

/**
 * A builder for creating KotlinPoet [ParameterSpec].
 */
@PicardDSLMarker
class KPParameterSpecBuilder : TypedSpec, DefaultKotlinPoetSpec() {
    /**
     * The name of the parameter.
     * This is a required field and must be set before building the [ParameterSpec].
     */
    override var type: TypeName? = null
    private var defaultValue: String? = null

    /**
     * Add the default value for the parameter.
     *
     * @param value The default value to be set for the parameter.
     */
    fun defaultValue(value: Any?) {
        defaultValue = value?.toString()
    }

    /**
     * Builds the [ParameterSpec] with the specified name, type, modifiers, and default value.
     * @return A [ParameterSpec] object representing the parameter.
     */
    @Suppress("SpreadOperator")
    fun build(): ParameterSpec {
        var spec = ParameterSpec.Companion
            .builder(
                requireNotNull(name) { "name must be set" },
                requireNotNull(type) { "type must be set" },
                *modifiers.toTypedArray()
            )

        if (defaultValue != null) spec = spec.defaultValue(defaultValue!!)

        return spec.build()
    }

    // Note: Parameter grouping utilities were removed to reduce API surface.
}
