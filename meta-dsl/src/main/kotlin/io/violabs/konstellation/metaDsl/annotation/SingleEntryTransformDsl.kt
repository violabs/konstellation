package io.violabs.konstellation.metaDsl.annotation

import kotlin.reflect.KClass

/**
 * Used to let the DSL Generator create a function with a primitive or
 * other object as the input. Incompatible types will need to be handled.
 * Requires a single constructor with the expected returnType.
 *
 * e.g. Duration(%N)
 */
annotation class SingleEntryTransformDsl<T : Any>(
    val inputType: KClass<T>,
    val transformTemplate: String = ""
)