package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT

/**
 * A builder for creating Kotlin Poets [LambdaTypeName].
 */
@PicardDSLMarker
class KPLambdaTypeNameBuilder : ParamSpecEnabled {
    /**
     * The receiver type of the lambda.
     * If null, the lambda is a top-level function.
     */
    var receiver: TypeName? = null
    /**
     * The return type of the lambda.
     * Defaults to [UNIT] if not specified.
     */
    var returnType: TypeName = UNIT
    override var params: MutableList<ParameterSpec> = mutableListOf()

    fun build(): TypeName {
        return LambdaTypeName.Companion.get(
            receiver = receiver,
            parameters = params,
            returnType = returnType
        )
    }
}