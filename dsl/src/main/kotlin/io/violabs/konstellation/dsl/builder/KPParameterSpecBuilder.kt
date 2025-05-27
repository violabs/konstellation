package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName

/**
 * A builder for creating Kotlin Poets [ParameterSpec].
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

    /**
     * A group of parameters that can be added to a DSL element.
     */
    class Group {
        val items: MutableList<ParameterSpec> = mutableListOf()

        /**
         * Adds a parameter to the list using a block to configure the [KPParameterSpecBuilder].
         * available accessors:
         * - [KPParameterSpecBuilder.defaultValue]
         * - [KPParameterSpecBuilder.type]
         *
         * @param block A lambda that configures the [KPParameterSpecBuilder] to create a [ParameterSpec].
         */
        fun param(
            block: KPParameterSpecBuilder.() -> Unit
        ): ParameterSpec {
            return KPParameterSpecBuilder().apply(block).build()
        }

        /**
         * Adds a [KPParameterSpecBuilder] to the list.
         * available accessors:
         * - [KPParameterSpecBuilder.defaultValue]
         * - [KPParameterSpecBuilder.type]
         *
         * @param block The [ParameterSpec] to be added to the list.
         */
        fun varargParam(
            block: KPParameterSpecBuilder.() -> Unit
        ): ParameterSpec {
            return KPParameterSpecBuilder()
                .apply {
                    name = "items"
                }
                .apply(block)
                .apply {
                    modifiers.add(KModifier.VARARG)
                }
                .build()
        }
    }
}