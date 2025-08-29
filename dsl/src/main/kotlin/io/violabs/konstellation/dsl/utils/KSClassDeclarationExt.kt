package io.violabs.konstellation.dsl.utils

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.violabs.konstellation.metaDsl.annotation.GeneratedDsl
import io.violabs.konstellation.metaDsl.annotation.MapGroupType

/**
 * Extension functions for [KSClassDeclaration] to check if it is a DSL class
 * and to retrieve group type information from its annotations.
 */
fun KSClassDeclaration?.isGroupDsl(): Boolean = this?.annotations
    ?.filter { it.shortName.asString() == GeneratedDsl::class.simpleName }
    ?.any { annotation ->
        annotation
            .arguments
            .firstOrNull { it.name?.asString() == GeneratedDsl::withListGroup.name }
            ?.value == true
    }
    ?: false

/**
 * Extension function to retrieve the group type from the annotations of a [KSClassDeclaration].
 */
fun KSClassDeclaration?.mapGroupType(): MapGroupType? = this?.annotations
    ?.filter { it.shortName.asString() == GeneratedDsl::class.simpleName }
    ?.flatMap(KSAnnotation::arguments)
    ?.firstOrNull { it.name?.asString() == GeneratedDsl::withMapGroup.name }
    ?.let { MapGroupType.valueOf(it.value.toString().uppercase()) }
