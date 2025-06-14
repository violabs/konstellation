package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.ClassName
import io.violabs.konstellation.dsl.utils.VLoggable

/**
 * Handles adding annotations to DSL builders.
 */
class AnnotationDecorator : VLoggable {
    init {
        logger.enableDebug()
    }

    /**
     * Creates a DSL marker with the given class, if available.
     * @param dslMarkerClass The fully qualified name of the DSL marker class.
     * @return A ClassName object representing the DSL marker class, or null if the class is not available.
     */
    fun createDslMarkerIfAvailable(dslMarkerClass: String?): ClassName? {
        if (dslMarkerClass == null) return null

        logger.debug("DSL Marker added", tier = 1, branch = true)
        val split = dslMarkerClass.split(".")
        val dslMarkerPackageName = split.subList(0, split.size - 1).joinToString(".")
        val dslMarkerSimpleName = split.last()
        return ClassName(dslMarkerPackageName, dslMarkerSimpleName)
    }
}
