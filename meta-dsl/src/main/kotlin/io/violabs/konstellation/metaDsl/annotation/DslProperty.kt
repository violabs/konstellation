package io.violabs.konstellation.metaDsl.annotation

/**
 * Annotation to configure DSL property generation for list and map properties.
 *
 * By default, list and map properties will generate both vararg and provider functions:
 * - Vararg: `names(vararg name: String)` - allows passing multiple items directly
 * - Provider: `names(provider: () -> List<String>)` - allows passing a lambda that returns the collection
 *
 * Use this annotation to disable either or both of these generated functions.
 *
 * Example usage:
 * ```kotlin
 * @GeneratedDsl
 * data class Person(
 *     val name: String,
 *
 *     // Generates both vararg and provider (default)
 *     val tags: List<String>,
 *
 *     // Only generates provider function
 *     @DslProperty(withVararg = false)
 *     val aliases: List<String>,
 *
 *     // Only generates vararg function
 *     @DslProperty(withProvider = false)
 *     val nicknames: List<String>,
 *
 *     // Generates neither (property must be set directly)
 *     @DslProperty(withVararg = false, withProvider = false)
 *     val ids: List<Int>
 * )
 * ```
 *
 * @property withVararg Whether to generate a vararg function. Default is true.
 * @property withProvider Whether to generate a provider function. Default is true.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class DslProperty(
    val withVararg: Boolean = true,
    val withProvider: Boolean = true
)
