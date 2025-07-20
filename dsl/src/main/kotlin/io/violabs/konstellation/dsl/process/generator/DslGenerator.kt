package io.violabs.konstellation.dsl.process.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.toClassName
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
import io.violabs.konstellation.metaDsl.annotation.GeneratedDsl
import io.violabs.konstellation.metaDsl.annotation.SingleEntryTransformDsl
import kotlin.reflect.KClass

/**
 * Interface for generating DSL builders and root DSL accessors.
 * This interface defines the contract for generating DSL files based on domain configurations.
 * @param propertySchemaFactory The type of the property schema factory adapter.
 * @param builderGenerator The generator for DSL builders.
 * @param rootDslAccessorGenerator The generator for root DSL accessors.
 */
interface DslGenerator<PARAM_ADAPTER : PropertySchemaFactoryAdapter, PROP_ADAPTER : DomainProperty> : VLoggable {
    val propertySchemaFactory: PropertySchemaFactory<PARAM_ADAPTER, PROP_ADAPTER>
    val builderGenerator: BuilderGenerator
    val rootDslAccessorGenerator: RootDslAccessorGenerator
    override fun logId(): String? = DslGenerator::class.simpleName

    /**
     * Generates the DSL builders and root DSL accessors based on the provided resolver and code generator.
     * This method will process the annotations and generate the necessary files.
     *
     * @param resolver The KSP resolver used to access symbols and annotations.
     * @param codeGenerator The KSP code generator used to write the generated files.
     * @param options Compile-time options defined in the host project, such as classpath and marker class.
     */
    fun generate(resolver: Resolver, codeGenerator: CodeGenerator, options: Map<String, String?> = emptyMap())
}

/**
 * Default implementation of [DslGenerator].
 * This class provides the default behavior for generating DSL builders and root DSL accessors.
 * @property propertySchemaFactory The factory for creating property schemas.
 * @property builderGenerator The generator for DSL builders.
 * @property rootDslAccessorGenerator The generator for root DSL accessors.
 */
class DefaultDslGenerator(
    override val propertySchemaFactory: DefaultPropertySchemaFactory = DefaultPropertySchemaFactory(),
    override val builderGenerator: DefaultBuilderGenerator = DefaultBuilderGenerator(),
    override val rootDslAccessorGenerator: DefaultRootDslAccessorGenerator = DefaultRootDslAccessorGenerator()
) : DslGenerator<DefaultPropertySchemaFactoryAdapter, DefaultDomainProperty> {

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

        val generatedBuilderDSL: List<KSClassDeclaration> = getGeneratedDslAnnotation(resolver)

        val singleEntryTransformByClassName: Map<String, KSClassDeclaration> =
            getSingleEntryTransformByClassName(resolver)

        generatedBuilderDSL.forEach { domain ->
            builderGenerator.generate(
                codeGenerator,
                domain,
                builderConfig,
                singleEntryTransformByClassName,
                domain.isDebug()
            )
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

    /**
     * Retrieves the generated DSL annotations from the resolver.
     * This method fetches all class declarations annotated with [GeneratedDsl].
     *
     * @param resolver The KSP resolver used to access symbols and annotations.
     * @return A list of KSClassDeclaration that are annotated with [GeneratedDsl].
     */
    private fun getGeneratedDslAnnotation(resolver: Resolver): List<KSClassDeclaration> {
        return getClassDeclarationByAnnotation(resolver, GeneratedDsl::class)
    }

    /**
     * Retrieves the single entry transform DSL annotations from the resolver.
     * This method fetches all class declarations annotated with [SingleEntryTransformDsl] and
     * maps them by their class name.
     *
     * @param resolver The KSP resolver used to access symbols and annotations.
     * @return A map of class names to their corresponding KSClassDeclaration for single entry transformations.
     */
    private fun getSingleEntryTransformByClassName(resolver: Resolver): Map<String, KSClassDeclaration> {
        return getClassDeclarationByAnnotation(resolver, SingleEntryTransformDsl::class)
            .associateBy { it.toClassName().toString() }
    }

    /**
     * Retrieves class declarations annotated with a specific annotation.
     * This method filters the symbols in the resolver to find class declarations that are
     * annotated with the specified annotation.
     *
     * @param resolver The KSP resolver used to access symbols and annotations.
     * @param klass The KClass representing the annotation to filter by.
     * @return A list of KSClassDeclaration that are annotated with the specified annotation.
     */
    private fun getClassDeclarationByAnnotation(resolver: Resolver, klass: KClass<*>): List<KSClassDeclaration> {
        return resolver
            .getSymbolsWithAnnotation(klass.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .onEach { logger.debug("Found ${Colors.yellow("@${klass.simpleName}")} on ${it.simpleName.asString()}") }
            .toList()
    }

    /**
     * Checks if the class declaration
     * is a root DSL class.
     *
     * @receiver [KSClassDeclaration] The KSClassDeclaration to check.
     * @return Boolean indicating whether the class is a root DSL class.
     */
    private fun KSClassDeclaration.isRootDsl(): Boolean = this
        .annotations
        .filter { it.shortName.asString() == GeneratedDsl::class.simpleName }
        .any { annotation ->
            annotation
                .arguments
                .firstOrNull { it.name?.asString() == GeneratedDsl::isRoot.name }
                ?.value == true
        }

    private fun KSClassDeclaration.isDebug(): Boolean = this
        .annotations
        .filter { it.shortName.asString() == GeneratedDsl::class.simpleName }
        .any { annotation ->
            annotation
                .arguments
                .firstOrNull { it.name?.asString() == GeneratedDsl::debug.name }
                ?.value == true
        }
}

