package io.violabs.konstellation.dsl.domain

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName

interface DomainProperty {
    val type: TypeName?

    fun simpleName(): String
    fun continueBranch(): Boolean
    fun singleEntryTransformString(): String?
}

data class DefaultDomainProperty(
    val index: Int,
    val lastIndex: Int,
    val prop: KSPropertyDeclaration,
    val singleEntryTransformMap: Map<String, KSClassDeclaration>
) : DomainProperty {
    override val type: TypeName? = prop.type.toTypeName().copy(nullable = false)

    override fun simpleName(): String {
        return prop.simpleName.asString()
    }

    override fun continueBranch(): Boolean = index != lastIndex

    fun singleEntryTransform(): KSClassDeclaration? = singleEntryTransformMap[type.toString()]

    override fun singleEntryTransformString(): String? = singleEntryTransform()?.toString()
}