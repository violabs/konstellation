package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec

/**
 * An interface that enables the addition of parameters to a DSL element.
 */
interface ParamSpecEnabled {
    /**
     * A mutable list of [ParameterSpec] that can be modified by the DSL builder.
     */
    var params: MutableList<ParameterSpec>

    /**
     * Adds a group of parameters to the DSL element.
     * available accessors:
     * - [KPParameterSpecBuilder.Group.items]
     * - [KPParameterSpecBuilder.Group.param]
     *
     * @param block A lambda that configures the [KPParameterSpecBuilder.Group].
     */
    fun params(block: KPParameterSpecBuilder.Group.() -> Unit) {
        params = KPParameterSpecBuilder.Group().apply(block).items
    }

    /**
     * Adds a parameter to the list of parameters.
     * Available accessors:
     * - [KPParameterSpecBuilder.defaultValue]
     * - [KPParameterSpecBuilder.type]
     * - [DefaultKotlinPoetSpec.modifiers]
     * @param block A lambda that configures the [KPParameterSpecBuilder] to create a [ParameterSpec].
     * @return A [ParameterSpec] object representing the parameter.
     */
    fun param(
        block: KPParameterSpecBuilder.() -> Unit
    ): ParameterSpec {
        return KPParameterSpecBuilder().apply(block).build().also { params.add(it) }
    }

    /**
     * Adds a parameter with a vararg modifier to the list of parameters.
     * Available accessors:
     * - [KPParameterSpecBuilder.defaultValue]
     * - [KPParameterSpecBuilder.type]
     * - [DefaultKotlinPoetSpec.modifiers]
     *
     * @param block A lambda that configures the [KPParameterSpecBuilder] to create a [ParameterSpec].
     * @return A [ParameterSpec] object representing the vararg parameter.
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
            .also { params.add(it) }
    }
}
