package io.violabs.konstellation.dsl

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Provider for the DSL symbol processor.
 */
@AutoService(SymbolProcessorProvider::class)
class DslProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) =
        DslProcessor(environment.codeGenerator, environment.options)
}
