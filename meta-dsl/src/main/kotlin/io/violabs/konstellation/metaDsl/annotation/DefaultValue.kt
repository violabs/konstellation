package io.violabs.konstellation.metaDsl.annotation

/**
 * Annotation to specify a default value for a property in the generated DSL.
 *
 * @property value The default value as a string.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class DefaultValue(
    val value: String,
    val packageName: String = "kotlin",
    val className: String = "String"
)
