package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.ClassName

/**
 * A group of annotations that can be applied to a DSL element.
 */
@PicardDSLMarker
class AnnotationGroup {
    val annotationNames: MutableList<ClassName> = mutableListOf()

    /**
     * Adds an annotation to the list by package name and annotation simple name.
     * Nested classes have not been verified, so this method is primarily for simple annotations.
     *
     * @param packageName The package name of the annotation.
     * @param annotationSimpleName The simple name of the annotation.
     */
    fun annotation(packageName: String, annotationSimpleName: String) {
        annotationNames.add(ClassName(packageName, annotationSimpleName))
    }

    /**
     * Adds an annotation to the list using a provider function that returns a ClassName.
     * If the provider returns null, the annotation is not added.
     *
     * @param provider A function that returns a ClassName or null.
     */
    fun annotation(provider: () -> ClassName?) {
        provider()?.let { annotationNames.add(it) }
    }

    /**
     * Adds an annotation to the list using a ClassName object.
     *
     * @param className The ClassName of the annotation to add.
     */
    fun annotation(className: ClassName) {
        annotationNames.add(className)
    }
}