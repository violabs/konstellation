package io.violabs.konstellation.dsl.process.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.toClassName
import io.violabs.konstellation.metaDsl.annotation.GeneratedDsl
import io.violabs.konstellation.metaDsl.annotation.SingleEntryTransformDsl
import io.violabs.konstellation.dsl.domain.BuilderConfig
import io.violabs.konstellation.dsl.domain.DefaultDomainProperty
import io.violabs.konstellation.dsl.domain.DomainProperty
import io.violabs.konstellation.dsl.process.propSchema.DefaultPropertySchemaFactory
import io.violabs.konstellation.dsl.process.propSchema.DefaultPropertySchemaFactoryAdapter
import io.violabs.konstellation.dsl.process.propSchema.PropertySchemaFactory
import io.violabs.konstellation.dsl.process.propSchema.PropertySchemaFactoryAdapter
import io.violabs.konstellation.dsl.process.root.DefaultRootDslAccessorGenerator
import io.violabs.konstellation.dsl.process.root.RootDslAccessorGenerator
import io.violabs.konstellation.dsl.utils.Colors
import io.violabs.konstellation.dsl.utils.VLoggable
import kotlin.reflect.KClass

interface DslGenerator<PARAM_ADAPTER : PropertySchemaFactoryAdapter, PROP_ADAPTER : DomainProperty> : VLoggable {
    val propertySchemaFactory: PropertySchemaFactory<PARAM_ADAPTER, PROP_ADAPTER>
    val builderGenerator: BuilderGenerator
    val rootDslAccessorGenerator: RootDslAccessorGenerator
    override fun logId(): String? = DslGenerator::class.simpleName

    fun generate(resolver: Resolver, codeGenerator: CodeGenerator, options: Map<String, String?> = emptyMap())
}

class DefaultDslGenerator(
    override val propertySchemaFactory: DefaultPropertySchemaFactory = DefaultPropertySchemaFactory(),
    override val builderGenerator: DefaultBuilderGenerator = DefaultBuilderGenerator(),
    override val rootDslAccessorGenerator: DefaultRootDslAccessorGenerator = DefaultRootDslAccessorGenerator()
) : DslGenerator<DefaultPropertySchemaFactoryAdapter, DefaultDomainProperty> {
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
        val builderConfig = BuilderConfig(options, logger)

        if (builderConfig.isIgnored) {
            logger.warn(
                "------------------------ [SKIP] GENERATE for project: ${builderConfig.projectRootClasspath}",
                tier = 0
            )
            return
        }

        logger.debug("------------------------ GENERATE for project: ${builderConfig.projectRootClasspath}", tier = 0)
        builderConfig.printDebug()

        val generatedBuilderDSL = getGeneratedDslAnnotation(resolver)

        val singleEntryTransformByClassName: Map<String, KSClassDeclaration> =
            getSingleEntryTransformByClassName(resolver)

        generatedBuilderDSL.forEach { domain ->
            builderGenerator.generate(codeGenerator, domain, builderConfig, singleEntryTransformByClassName)
        }

        val rootClasses = generatedBuilderDSL
            .filter { it.isRootDsl() }
            .toList()

        if (rootClasses.isEmpty()) {
            logger.debug("No root classes found.")
            return
        }
        rootDslAccessorGenerator.generate(codeGenerator, rootClasses, builderConfig)
    }

    private fun getGeneratedDslAnnotation(resolver: Resolver): List<KSClassDeclaration> {
        return getClassDeclarationByAnnotation(resolver, GeneratedDsl::class)
    }

    private fun getSingleEntryTransformByClassName(resolver: Resolver): Map<String, KSClassDeclaration> {
        return getClassDeclarationByAnnotation(resolver, SingleEntryTransformDsl::class)
            .associateBy { it.toClassName().toString() }
    }

    private fun getClassDeclarationByAnnotation(resolver: Resolver, klass: KClass<*>): List<KSClassDeclaration> {
        return resolver
            .getSymbolsWithAnnotation(klass.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .onEach { logger.debug("Found ${Colors.yellow("@${klass.simpleName}")} on ${it.simpleName.asString()}") }
            .toList()
    }

    private fun KSClassDeclaration.isRootDsl(): Boolean = this
        .annotations
        .filter { it.shortName.asString() == GeneratedDsl::class.simpleName }
        .any { annotation ->
            annotation
                .arguments
                .firstOrNull { it.name?.asString() == GeneratedDsl::isRoot.name }
                ?.value == true
        }
}

