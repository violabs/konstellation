package io.violabs.konstellation.metaDsl

interface CoreDslBuilder<T> {
    fun build(): T
}