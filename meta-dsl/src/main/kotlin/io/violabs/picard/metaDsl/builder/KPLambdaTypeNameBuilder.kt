package io.violabs.picard.metaDsl.builder

import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT

@PicardDSLMarker
class KPLambdaTypeNameBuilder : ParamSpecEnabled {
    var receiver: TypeName? = null
    private var parameters: MutableList<ParameterSpec> = mutableListOf()
    var returnType: TypeName = UNIT
    override var params: MutableList<ParameterSpec> = mutableListOf()

    fun build(): TypeName {
        return LambdaTypeName.Companion.get(
            receiver = receiver,
            parameters = parameters,
            returnType = returnType
        )
    }
}