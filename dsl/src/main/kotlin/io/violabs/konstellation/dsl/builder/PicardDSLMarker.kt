package io.violabs.konstellation.dsl.builder

/**
 * A marker annotation for DSLs in the Picard library.
 * This annotation is used to restrict the scope of DSL builders
 * and prevent accidental misuse of DSL elements outside their intended context.
 */
@DslMarker
annotation class PicardDSLMarker