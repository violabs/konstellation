package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.ClassName
import io.violabs.konstellation.dsl.utils.VLoggable

class AnnotationDecorator : VLoggable {
    init {
        logger.enableDebug()
    }

    fun createDslMarkerIfAvailable(dslMarkerClasspath: String?): ClassName? {
        if (dslMarkerClasspath == null) return null

        logger.debug("DSL Marker added", tier = 1, branch = true)
        val split = dslMarkerClasspath.split(".")
        val dslMarkerPackageName = split.subList(0, split.size - 1).joinToString(".")
        val dslMarkerSimpleName = split.last()
        return ClassName(dslMarkerPackageName, dslMarkerSimpleName)
    }
}