package io.violabs.konstellation.metaDsl.annotation

import kotlin.reflect.KClass

/**
 * Annotation to specify a default value for a property in the generated DSL.
 *
 * @property value The default value as a string.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class DefaultValue(val value: String, val classRef: KClass<*> = String::class)
