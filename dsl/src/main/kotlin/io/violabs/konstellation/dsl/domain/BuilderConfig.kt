package io.violabs.konstellation.dsl.domain

import io.violabs.konstellation.dsl.utils.Logger

class BuilderConfig(
    map: Map<String, String?>,
    private val logger: Logger
) {
    /**
     * Disables generation.
     */
    val isIgnored: Boolean = map["isIgnored"]?.toBoolean() ?: false

    /**
     * The project root class path [e.g. io.violabs.target.folder]
     */
    val projectRootClasspath: String = map["projectRootClasspath"]
        ?: throw RuntimeException("projectRootClasspath is missing")

    /**
     * The path to the project defined DslBuilder that extends [io.violabs.konstellation.dsl.CoreDslBuilder]
     */
    val dslBuilderClasspath: String = map["dslBuilderClasspath"]
        ?: throw RuntimeException("dslBuilderClasspath is missing")

    /**
     * The class path for where the generated root DSL file will go.
     * Defaults to [projectRootClasspath]
     */
    private val rootDslFileClasspath: String? = map["rootDslFileClasspath"]

    /**
     * The class (with class path) of the marker class (e.g. io.violabs.exampleProj.ExampleProjMarkerClass
     */
    val dslMarkerClass: String? = map["dslMarkerClass"]

    /**
     * Returns the root DSL file classpath if provided or the default of the project root.
     */
    fun rootDslFileClasspath(): String = rootDslFileClasspath ?: projectRootClasspath

    fun printDebug() {
        logger.debug("rootDslFileClasspath: ${rootDslFileClasspath()}")
        logger.debug("dslBuilderClasspath: $dslBuilderClasspath")
        logger.debug("dslMarkerClass: $dslMarkerClass")
    }
}