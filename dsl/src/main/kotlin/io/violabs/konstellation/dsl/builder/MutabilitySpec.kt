package io.violabs.konstellation.dsl.builder

/**
 * A specification for mutability in DSL builders.
 */
internal interface MutabilitySpec {
    /**
     * Indicates whether the variable is mutable.
     * Defaults to true, meaning the variable can be changed.
     */
    var mutable: Boolean

    /**
     * Sets the mutability of the variable.
     * @param on If true, the variable is mutable; if false, it is immutable.
     */
    fun mutable(on: Boolean = true) {
        mutable = on
    }

    /**
     * Sets the variable to be mutable.
     * This is a convenience method for setting mutable to true.
     */
    fun variable() {
        mutable = true
    }

    /**
     * Sets the variable to be immutable.
     * This is a convenience method for setting mutable to false.
     */
    fun value() {
        mutable = false
    }

}
