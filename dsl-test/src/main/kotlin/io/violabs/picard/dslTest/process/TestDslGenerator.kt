package io.violabs.picard.dslTest.process

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.toClassName
import io.violabs.picard.common.Colors
import io.violabs.picard.common.VLoggable
import io.violabs.picard.metaDsl.annotation.GeneratedDsl
import io.violabs.picard.metaDsl.annotation.SingleEntryTransformDsl
import io.violabs.picard.metaDsl.config.BuilderConfig
import io.violabs.picard.metaDsl.process.DefaultDomainProperty
import io.violabs.picard.metaDsl.process.DefaultPropertySchemaFactory
import io.violabs.picard.metaDsl.process.DefaultPropertySchemaFactoryAdapter
import io.violabs.picard.metaDsl.process.DomainProperty
import io.violabs.picard.metaDsl.process.PropertySchemaFactory
import io.violabs.picard.metaDsl.process.PropertySchemaFactoryAdapter
import kotlin.reflect.KClass

interface TestDslGenerator<PARAM_ADAPTER : PropertySchemaFactoryAdapter, PROP_ADAPTER : DomainProperty> : VLoggable {
    override fun logId(): String? = TestDslGenerator::class.simpleName

    val propertySchemaFactory: PropertySchemaFactory<PARAM_ADAPTER, PROP_ADAPTER>
    val testGenerator: TestGenerator

    fun generate(resolver: Resolver, codeGenerator: CodeGenerator, options: Map<String, String?> = emptyMap())
}

class DefaultDslGenerator(
    override val propertySchemaFactory: DefaultPropertySchemaFactory = DefaultPropertySchemaFactory(),
    override val testGenerator: DefaultTestGenerator = DefaultTestGenerator()
) : TestDslGenerator<DefaultPropertySchemaFactoryAdapter, DefaultDomainProperty> {
    init {
        logger.enableDebug()
    }

    /**
     * This will generate Custom DSL Builders based on the annotation, as well as the
     * test classes that can verify the data.
     * Keywords:
     * - User Defined DSL: the use of the [GeneratedDsl] annotation to define a custom DSL
     * - DSL Builder: the engine that creates the DSL per use
     * - DSL Marker: the provided annotation for the used library (also restricts scope)
     * - Resolved Class: the user defined class that the [GeneratedDsl] will decorate
     * - Builder Class: the generated class that should match the specs of the Resolved Class
     * - Group Class: A list of builders - requires a specific implementation
     * - Map Group Class: a map of builders - requires a specific implementation
     * todo: List<List<*>>, Map<T, List<*>>, Map<T, Map<T, *>
     * @param resolver symbol resolver for ksp interaction
     * @param codeGenerator creates and manages files with ksp
     * @param options compile time options defined in the host project:
     * - **dslBuilderClasspath** ~required~ the classpath to the custom DslBuilder class
     * - **dslMarkerClass** ~required~ the class that contains a [DslMarker] to denote the host dsl name.
     */
    override fun generate(resolver: Resolver, codeGenerator: CodeGenerator, options: Map<String, String?>) {
        logger.debug("------- GENERATE", tier = 0)
        val builderConfig = BuilderConfig(options, logger)

        val generatedBuilderDSL = getGeneratedDslAnnotation(resolver)
        logger.debug("num found: ${generatedBuilderDSL.count()}", tier = 1, branch = true)

        val singleEntryTransformByClassName: Map<String, KSClassDeclaration> =
            getSingleEntryTransformByClassName(resolver)

        generatedBuilderDSL.forEach { domain ->
            testGenerator.generate(codeGenerator, domain, builderConfig, singleEntryTransformByClassName)
        }
    }

    private fun getGeneratedDslAnnotation(resolver: Resolver): Sequence<KSClassDeclaration> {
        return getClassDeclarationByAnnotation(resolver, GeneratedDsl::class)
    }

    private fun getSingleEntryTransformByClassName(resolver: Resolver): Map<String, KSClassDeclaration> {
        return getClassDeclarationByAnnotation(resolver, SingleEntryTransformDsl::class)
            .associateBy { it.toClassName().toString() }
    }

    private fun getClassDeclarationByAnnotation(resolver: Resolver, klass: KClass<*>): Sequence<KSClassDeclaration> {
        return resolver
            .getSymbolsWithAnnotation(klass.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .onEach { logger.debug("Found ${Colors.yellow("@${klass.simpleName}")} on ${it.simpleName.asString()}") }
    }
}