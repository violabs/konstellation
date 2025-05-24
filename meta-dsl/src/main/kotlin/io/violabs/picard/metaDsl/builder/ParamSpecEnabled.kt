package io.violabs.picard.metaDsl.builder

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec

interface ParamSpecEnabled {
    var params: MutableList<ParameterSpec>


    fun params(block: KPParameterSpecBuilder.Group.() -> Unit) {
        params = KPParameterSpecBuilder.Group().apply(block).items
    }

    fun param(
        block: KPParameterSpecBuilder.() -> Unit
    ): ParameterSpec {
        return KPParameterSpecBuilder().apply(block).build().also { params.add(it) }
    }

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