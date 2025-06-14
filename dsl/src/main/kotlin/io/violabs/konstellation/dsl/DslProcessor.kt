package io.violabs.konstellation.dsl

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import io.violabs.konstellation.dsl.process.generator.DefaultDslGenerator

/**
 * Symbol processor for generating DSL code.
 */
class DslProcessor(
    private val codeGenerator: CodeGenerator,
    private val options: Map<String, String>
) : SymbolProcessor {
    /**
     * Processes the resolver to generate DSL code.
     *
     * @param resolver The KSP resolver used to access symbols and annotations.
     * @return An empty list as this processor does not produce any symbols.
     */
    override fun process(resolver: Resolver): List<KSAnnotated> {
        DefaultDslGenerator().generate(resolver, codeGenerator, options)

        return emptyList()
    }
}
