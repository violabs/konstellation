package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName

/**
 * A builder for creating Kotlin Poets [PropertySpec].
 */
@PicardDSLMarker
class KPPropertySpecBuilder : TypedSpec, MutabilitySpec, DefaultKotlinPoetSpec() {
    /**
     * The type of the property.
     * This is a required field and must be set before building the [PropertySpec].
     */
    override var type: TypeName? = null
    override var mutable: Boolean = true

    /**
     * The initial value of the property.
     */
    var initializer: CodeBlock? = null

    fun initializer(code: String) {
        initializer = CodeBlock.of(code)
    }

    /**
     * Sets the initial value of the property to null.
     */
    fun initNullValue() {
        initializer = CodeBlock.of("null")
    }

    /**
     * Builds the [PropertySpec] with the specified name, type, modifiers, and initializer.
     * @return A [PropertySpec] object representing the property.
     */
    @Suppress("SpreadOperator")
    fun build(): PropertySpec {
        var spec = PropertySpec
            .builder(
                requireNotNull(name) { "name must be set" },
                requireNotNull(type) { "type must be set" },
                *modifiers.toTypedArray()
            )
            .mutable(mutable)

        spec = initializer?.let { spec.initializer(it) } ?: spec

        return spec.build()
    }

    /**
     * A group of properties that can be added to a DSL element.
     * This is used to define properties for classes, objects, and other KotlinPoet constructs.
     */
    @PicardDSLMarker
    class Group {
        val items: MutableList<PropertySpec> = mutableListOf()

        /**
         * Adds a property to the list using a block to configure the [KPPropertySpecBuilder].
         * Available accessors:
         * - [KPPropertySpecBuilder.type]
         * - [KPPropertySpecBuilder.initializer]
         * - [KPPropertySpecBuilder.mutable]
         * - [KPPropertySpecBuilder.initNullValue]
         *
         * @param block A lambda that configures the [KPPropertySpecBuilder] to create a [PropertySpec].
         */
        fun add(block: KPPropertySpecBuilder.() -> Unit) {
            items.add(KPPropertySpecBuilder().apply(block).build())
        }

        /**
         * Adds a [PropertySpec] to the list.
         *
         * @param spec The [PropertySpec] to be added to the list.
         */
        fun add(spec: PropertySpec) {
            items.add(spec)
        }

        /**
         * Will use the [transformFn] on each item in the [list] and add the resulting
         * [PropertySpec] to the list.
         * You can use the extension of [List.addForEach] within this scope.
         */
        fun <T> addForEachIn(list: List<T>, transformFn: (T) -> PropertySpec) {
            list.forEach { add(transformFn(it)) }
        }

        /**
         * Will use the [transformFn] on each item int this list and add the resulting
         * [PropertySpec] to the items list.
         */
        fun <T> List<T>.addForEach(transformFn: (T) -> PropertySpec) = this.forEach { add(transformFn(it)) }
    }

    companion object {
        /**
         * A list of all access modifiers that can be used in Kotlin.
         * This includes public, protected, internal, and private.
         */
        val ALL_ACCESS_MODIFIERS = listOf(KModifier.PUBLIC, KModifier.PROTECTED, KModifier.INTERNAL, KModifier.PRIVATE)
    }
}
