package io.violabs.konstellation.metaDsl

/**
 * Marker interface for DSL builders in the core DSL.
 *
 * This interface is used to define a contract for building DSL objects.
 *
 * @param T The type of the object being built.
 */
interface CoreDslBuilder<T> {
    fun build(): T
}