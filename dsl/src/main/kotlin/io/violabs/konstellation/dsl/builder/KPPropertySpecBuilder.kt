package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName


@PicardDSLMarker
class KPPropertySpecBuilder : TypedSpec, MutabilitySpec, DefaultKotlinPoetSpec() {
    override var type: TypeName? = null
    override var mutable: Boolean = true
    var initializer: String? = null

    fun initNullValue() {
        initializer = "null"
    }

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

    @PicardDSLMarker
    class Group {
        val items: MutableList<PropertySpec> = mutableListOf()

        fun add(block: KPPropertySpecBuilder.() -> Unit) {
            items.add(KPPropertySpecBuilder().apply(block).build())
        }

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
        val ALL_ACCESS_MODIFIERS = listOf(KModifier.PUBLIC, KModifier.PROTECTED, KModifier.INTERNAL, KModifier.PRIVATE)
    }
}