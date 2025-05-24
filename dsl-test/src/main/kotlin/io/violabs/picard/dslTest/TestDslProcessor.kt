package io.violabs.picard.dslTest

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import io.violabs.picard.common.VLoggable
import io.violabs.picard.dslTest.process.DefaultDslGenerator

class TestDslProcessor(
    private val codeGenerator: CodeGenerator,
    private val options: Map<String, String>
) : SymbolProcessor, VLoggable {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.debug("Starting Test DSL Processor")
        DefaultDslGenerator().generate(resolver, codeGenerator, options)

        logger.debug("Finished Test DSL Processor")
        return emptyList()
    }
}