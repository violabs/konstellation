package io.violabs.konstellation.generateTest

import io.violabs.konstellation.metaDsl.CoreDslBuilder

@TestDslMarker
interface DslBuilder<T> : CoreDslBuilder<T> {
    override fun build(): T
}