package io.violabs.konstellation.dsl.process.propSchema

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import io.violabs.konstellation.metaDsl.annotation.GeneratedDsl

interface PropertySchemaFactoryAdapter {
    val propName: String
    val actualPropTypeName: TypeName
    val hasSingleEntryTransform: Boolean
    val transformTemplate: String?
    val transformType: TypeName?
    val hasNullableAssignment: Boolean
    val propertyNonNullableClassName: ClassName?
    val hasGeneratedDslAnnotation: Boolean
    val propertyClassDeclarationQualifiedName: String?
    val propertyClassDeclaration: KSClassDeclaration?
    val isGroupElement: Boolean
    val groupElementClassName: ClassName?
    val groupElementClassDeclaration: KSClassDeclaration?
    var mapDetails: MapDetails?
    val mapValueClassDeclaration: KSClassDeclaration?

    fun mapDetails(): MapDetails? = null

    fun nonNullablePropTypeName(): TypeName = actualPropTypeName.copy(nullable = false)

    interface MapDetails {
        val mapGroupType: GeneratedDsl.MapGroupType
        val keyType: TypeName
        val valueType: TypeName

        fun valueClass(): ClassName = valueType.copy(nullable = false) as ClassName
    }
}