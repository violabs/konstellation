package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName

/**
 * A DSL builder for creating KotlinPoet [FunSpec] objects.
 */
@PicardDSLMarker
class KPFunSpecBuilder : ParamSpecEnabled {
    /**
     * The name of the function.
     * This is a required field and must be set before building the [FunSpec].
     */
    var funName: String? = null
    /**
     * The return type of the function.
     * This is optional and can be set to null if the function does not return a value.
     */
    var returns: TypeName? = null
    /**
     * The KDoc documentation for the function.
     * This is optional and can be set to null if no documentation is needed.
     */
    var kdoc: String? = null
    private var annotations: List<ClassName> = mutableListOf()
    private var overridden: Boolean = false
    private var statements: MutableList<KPStatement> = mutableListOf()
    override var params: MutableList<ParameterSpec> = mutableListOf()

    /**
     * Sets whether the function is overridden.
     * @param on If true, the function will be marked as overridden. default is true
     */
    fun override(on: Boolean = true) {
        overridden = on
    }

    /**
     * sets the kdoc for the function.
     *
     * @param text The KDoc text to be set for the function.
     */
    fun kdoc(text: String) {
        kdoc = text
    }

    /**
     * Adds annotations to the function.
     * Available accessors:
     * - [AnnotationGroup.annotation]
     * - [AnnotationGroup.annotationNames]
     *
     * @param block A lambda that configures the [AnnotationGroup] to add annotations.
     */
    fun annotations(block: AnnotationGroup.() -> Unit) {
        this.annotations = AnnotationGroup().apply(block).annotationNames
    }

    /**
     * Adds statements to the function.
     * Available accessors:
     * - [KPStatement.Group.items]
     * - [KPStatement.Group.addLine]
     *
     * @param block A lambda that configures the [KPStatement.Group] to add statements.
     */
    fun statements(block: KPStatement.Group.() -> Unit) {
        statements = KPStatement.Group().apply(block).items
    }

    /**
     * Builds the [FunSpec] object with the specified properties.
     *
     * @return A [FunSpec] object representing the function with the specified name, parameters, return type, KDoc, annotations, and statements.
     */
    fun build(): FunSpec {
        val name = requireNotNull(funName) { "Fun - funName must be set" }
        var spec = FunSpec.builder(name)

        for (param in params) {
            spec = spec.addParameter(param)
        }

        spec = returns?.let { spec.returns(it) } ?: spec

        if (overridden) spec = spec.addModifiers(KModifier.OVERRIDE)
        if (kdoc != null) spec = spec.addKdoc(kdoc!!)

        for (statement in statements) {
            spec = spec.addStatement(statement.statement, *statement.args.toTypedArray())
        }

        for (annotation in annotations) {
            spec = spec.addAnnotation(annotation)
        }

        return spec.build()
    }

    /**
     * A group of functions that can be added to a DSL element.
     * @property items A mutable list of [FunSpec] objects that can be added to the group.
     */
    @PicardDSLMarker
    class Group {
        val items: MutableList<FunSpec> = mutableListOf()

        /**
         * Adds a function to the group by applying the provided block to a new [KPFunSpecBuilder].
         * available accessors:
         * - [KPFunSpecBuilder.funName]
         * - [KPFunSpecBuilder.returns]
         * - [KPFunSpecBuilder.kdoc]
         * - [KPFunSpecBuilder.annotations]
         * - [KPFunSpecBuilder.override]
         * - [KPFunSpecBuilder.statements]
         *
         * @param block A lambda that configures the [KPFunSpecBuilder] to create a [FunSpec].
         */
        fun add(block: KPFunSpecBuilder.() -> Unit) {
            items.add(KPFunSpecBuilder().apply(block).build())
        }

        /**
         * Adds a [FunSpec] to the group.
         *
         * @param specs The [FunSpec] list to be added to the group.
         */
        fun addAll(specs: List<FunSpec>) {
            items.addAll(specs)
        }

        /**
         * Will use the [transformFn] on each item in the [list] and add the resulting
         * [PropertySpec] to the list.
         * You can use the extension of [List.addForEach] within this scope.
         */
        fun <T> addForEachIn(list: List<T>, transformFn: (T) -> List<FunSpec>) {
            list.forEach { addAll(transformFn(it)) }
        }

        /**
         * Will use the [transformFn] on each item int this list and add the resulting
         * [PropertySpec] to the items list.
         */
        fun <T> List<T>.addForEach(transformFn: (T) -> List<FunSpec>) = this.forEach { addAll(transformFn(it)) }
    }
}