package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.KModifier
import io.violabs.konstellation.dsl.exception.KonstellationException

/**
 * Base interface for KotlinPoet specifications.
 */
interface KotlinPoetSpec {
    /**
     * The name of the spec
     */
    var name: String?

    /**
     * A list of modifiers for this spec
     * @see [KModifier]
     */
    val modifiers: MutableList<KModifier>
}

abstract class DefaultKotlinPoetSpec : KotlinPoetSpec {
    override var name: String? = null
    override val modifiers: MutableList<KModifier> = mutableListOf()

    /**
     * Add a single access modifier to this spec.
     *
     * @param modifier The access modifier to add, e.g., [KModifier.PRIVATE], [KModifier.PUBLIC].
     * @throws IllegalArgumentException if an access modifier has already been set
     */
    fun accessModifier(modifier: KModifier) {
        val existing = modifiers.firstOrNull { it in KPPropertySpecBuilder.Companion.ALL_ACCESS_MODIFIERS }
        if (existing != null) {
            throw KonstellationException("access modifier already set to $existing")
        }

        modifiers.add(modifier)
    }

    /**
     * Add a private access modifier to this spec.
     *
     * @return This spec with the private access modifier added.
     */
    fun private() = accessModifier(KModifier.PRIVATE)

    /**
     * Add a protected access modifier to this spec.
     *
     * @return This spec with the protected access modifier added.
     */
    fun protected() = accessModifier(KModifier.PROTECTED)

    /**
     * Add a public access modifier to this spec.
     *
     * @return This spec with the public access modifier added.
     */
    fun public() = accessModifier(KModifier.PUBLIC)
}
