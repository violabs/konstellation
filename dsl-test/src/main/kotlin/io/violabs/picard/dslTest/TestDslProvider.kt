package io.violabs.picard.dslTest

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

@AutoService(SymbolProcessorProvider::class)
class TestDslProvider : SymbolProcessorProvider {
    override fun create(env: SymbolProcessorEnvironment) =
        TestDslProcessor(env.codeGenerator, env.options)
}
