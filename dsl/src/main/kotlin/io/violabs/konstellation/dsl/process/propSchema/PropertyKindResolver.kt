package io.violabs.konstellation.dsl.process.propSchema

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.ClassName
import kotlin.reflect.KClass

/**
 * Compact classification for a property based on adapter metadata.
 * Keeps branching logic out of the factory for readability and testability.
 */
enum class ResolvedPropKind {
    SINGLE_TRANSFORM,
    BUILDER,
    BOOLEAN_DEFAULT,
    DEFAULT_PRIMITIVE,
    MAP_GROUP,
    MAP,
    LIST_GROUP,
    LIST,
    DEFAULT_FALLBACK
}

object PropertyKindResolver {
    private val defaultTypeNames = setOf(CHAR, STRING, BYTE, SHORT, INT, LONG, DOUBLE, FLOAT)

    fun <T : PropertySchemaFactoryAdapter> resolve(adapter: T): ResolvedPropKind {
        // Annotations-based shortcuts take precedence
        if (adapter.hasSingleEntryTransform) return ResolvedPropKind.SINGLE_TRANSFORM

        val propertyNonNullableClassName: ClassName? = adapter.propertyNonNullableClassName
        val hasGeneratedDSLAnnotation = adapter.hasGeneratedDslAnnotation
        if (hasGeneratedDSLAnnotation && propertyNonNullableClassName != null) {
            return ResolvedPropKind.BUILDER
        }

        val nonNullPropType: TypeName = adapter.nonNullablePropTypeName()

        // Simple boolean
        if (BOOLEAN == nonNullPropType) return ResolvedPropKind.BOOLEAN_DEFAULT

        // Kotlin primitives/strings
        if (nonNullPropType in defaultTypeNames) return ResolvedPropKind.DEFAULT_PRIMITIVE

        // Collections
        if (isCollectionType(adapter, MAP, Map::class)) {
            val mapDetails = adapter.mapDetails()
            return if (mapDetails?.mapGroupType in io.violabs.konstellation.metaDsl.annotation.MapGroupType.ACTIVE_TYPES) {
                ResolvedPropKind.MAP_GROUP
            } else {
                ResolvedPropKind.MAP
            }
        }

        if (isCollectionType(adapter, LIST, List::class)) {
            return if (adapter.isGroupElement) ResolvedPropKind.LIST_GROUP else ResolvedPropKind.LIST
        }

        return ResolvedPropKind.DEFAULT_FALLBACK
    }

    private fun isCollectionType(
        adapter: PropertySchemaFactoryAdapter,
        expectedType: TypeName,
        expectedClass: KClass<*>
    ): Boolean {
        val nonNullPropType = adapter.nonNullablePropTypeName()
        val isRawCollection = nonNullPropType is ParameterizedTypeName && nonNullPropType.rawType == expectedType
        val isQualifiedCollection = adapter.propertyClassDeclarationQualifiedName == expectedClass.qualifiedName
        return isRawCollection || isQualifiedCollection
    }
}
