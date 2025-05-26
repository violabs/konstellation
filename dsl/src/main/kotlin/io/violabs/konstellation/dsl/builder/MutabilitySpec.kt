package io.violabs.konstellation.dsl.builder

internal interface MutabilitySpec {
    var mutable: Boolean

    fun mutable(on: Boolean = true) {
        mutable = on
    }

    fun variable() {
        mutable = true
    }

    fun value() {
        mutable = false
    }

}