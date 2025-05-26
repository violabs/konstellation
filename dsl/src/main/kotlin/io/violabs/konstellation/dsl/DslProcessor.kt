package io.violabs.konstellation.dsl

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import io.violabs.konstellation.dsl.process.generator.DefaultDslGenerator

class DslProcessor(
    private val codeGenerator: CodeGenerator,
    private val options: Map<String, String>
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        DefaultDslGenerator().generate(resolver, codeGenerator, options)

        return emptyList()
    }
}