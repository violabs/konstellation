package io.violabs.konstellation.dsl.process

import io.violabs.konstellation.dsl.schema.DslPropSchema

/**
 * Centralizes computation of validation flags and related imports for generated files.
 */
object ValidationAnalyzer {
    data class Result(
        val requiresNotNull: Boolean,
        val requiresCollectionNotEmpty: Boolean,
        val requiresMapNotEmpty: Boolean,
        val defaultValueImports: Set<String>
    )

    fun analyze(schemas: List<DslPropSchema>): Result {
        val requiresNotNull = schemas.any { param -> !param.nullableAssignment && param.verifyNotNull }

        val requiresCollectionNotEmpty = schemas.any { param ->
            !param.nullableAssignment && param.verifyNotEmpty && param.isCollection()
        }

        val requiresMapNotEmpty = schemas.any { param ->
            !param.nullableAssignment && param.verifyNotEmpty && param.isMap()
        }

        val defaultValueImports: Set<String> = schemas
            .mapNotNull { it.defaultValue?.importString() }
            .toSet()

        return Result(
            requiresNotNull,
            requiresCollectionNotEmpty,
            requiresMapNotEmpty,
            defaultValueImports
        )
    }
}

