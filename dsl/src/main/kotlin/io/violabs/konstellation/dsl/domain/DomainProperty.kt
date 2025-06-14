package io.violabs.konstellation.dsl.domain

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Represents a property in a domain model.
 */
interface DomainProperty {
    val type: TypeName?

    fun simpleName(): String
    fun continueBranch(): Boolean
    fun singleEntryTransformString(): String?
}

/**
 * Default implementation of [DomainProperty].
 *
 * @param index The index of the property in the domain.
 * @param lastIndex The last index of the property in the domain.
 * @param prop The KSP property declaration representing the domain property.
 * @param singleEntryTransformMap A map of single entry transforms by type name.
 */
data class DefaultDomainProperty(
    val index: Int,
    val lastIndex: Int,
    val prop: KSPropertyDeclaration,
    val singleEntryTransformMap: Map<String, KSClassDeclaration>
) : DomainProperty {
    /**
     * The type of the property, converted to a KotlinPoet TypeName.
     * The type is non-nullable by default.
     */
    override val type: TypeName? = prop.type.toTypeName().copy(nullable = false)

    /**
     * Returns the simple name of the property.
     */
    override fun simpleName(): String {
        return prop.simpleName.asString()
    }

    /**
     * Checks if there are more properties to process in the domain.
     * Returns true if the current index is not the last index.
     */
    override fun continueBranch(): Boolean = index != lastIndex

    /**
     * Retrieves the single entry transform class declaration for the property's type.
     * Returns null if no transform is found for the type.
     */
    fun singleEntryTransform(): KSClassDeclaration? = singleEntryTransformMap[type.toString()]

    /**
     * Returns the string representation of the single entry transform class declaration
     */
    override fun singleEntryTransformString(): String? = singleEntryTransform()?.toString()
}
