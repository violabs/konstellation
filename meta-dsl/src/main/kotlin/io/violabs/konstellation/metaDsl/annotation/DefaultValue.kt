package io.violabs.konstellation.metaDsl.annotation

import kotlin.reflect.KClass

/**
 * Annotation to specify a default value for a property in the generated DSL.
 *
 * @property value The default value as a string.
 * @property classRef The KClass reference indicating the type of the default value. Defaults to String::class.
 *                    If import is needed, use the fully qualified name.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DefaultValue(
    val value: String,
    val classRef: KClass<*> = String::class
)
