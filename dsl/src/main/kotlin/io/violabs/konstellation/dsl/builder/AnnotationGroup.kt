package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.ClassName

@PicardDSLMarker
class AnnotationGroup {
    val annotationNames: MutableList<ClassName> = mutableListOf()

    fun annotation(packageName: String, annotationSimpleName: String) {
        annotationNames.add(ClassName(packageName, annotationSimpleName))
    }

    fun annotation(provider: () -> ClassName?) {
        provider()?.let { annotationNames.add(it) }
    }

    fun annotation(className: ClassName) {
        annotationNames.add(className)
    }
}