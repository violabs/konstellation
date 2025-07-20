package io.violabs.konstellation.dsl.process.propSchema

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.CodeBlock
import io.violabs.konstellation.dsl.domain.DefaultDomainProperty
import io.violabs.konstellation.dsl.domain.DefaultPropertyValue
import io.violabs.konstellation.dsl.domain.DomainConfig
import io.violabs.konstellation.dsl.domain.DomainProperty
import io.violabs.konstellation.dsl.schema.DslPropSchema
import io.violabs.konstellation.dsl.utils.Colors
import io.violabs.konstellation.dsl.utils.VLoggable
import io.violabs.konstellation.metaDsl.annotation.DefaultValue
import kotlin.reflect.KClass

/**
 * Service to handle the conversion of domain properties into DSL property schemas.
 */
interface PropertySchemaService<FACTORY_ADAPTER : PropertySchemaFactoryAdapter, PROP_ADAPTER : DomainProperty> :
    VLoggable {
    override fun logId(): String? = PropertySchemaService::class.simpleName

    fun getParamsFromDomain(domainConfig: DomainConfig): List<DslPropSchema>
}

/**
 * Default implementation of [PropertySchemaService] that
 * uses [DefaultPropertySchemaFactory] to create property schemas.
 * It converts domain properties into DSL property schemas.
 */
class DefaultPropertySchemaService(
    private val propertySchemaFactory: DefaultPropertySchemaFactory = DefaultPropertySchemaFactory()
) : PropertySchemaService<DefaultPropertySchemaFactoryAdapter, DefaultDomainProperty> {
    override fun getParamsFromDomain(domainConfig: DomainConfig): List<DslPropSchema> {
        val domain = domainConfig.domain
        val lastIndex = domain.getAllProperties().count() - 1

        return domain
            .getAllProperties()
            .mapIndexed { i, prop ->
                val defaultValue = prop.extractDefaultPropertyValue()

                logger.debug(
                    "Property '${prop.simpleName.asString()}' has ${Colors.yellow("@DefaultValue")}: $defaultValue",
                    tier = 2, branch = true
                )

                DefaultDomainProperty(
                    i, lastIndex,
                    prop,
                    domainConfig.singleEntryTransformByClassName,
                    defaultValue
                )
            }
            .map(propertySchemaFactory::createPropertySchemaFactoryAdapter)
            .map { propertySchemaFactory.determinePropertySchema(it, debug = logger.debugEnabled()) }
            .toList()
    }


    /**
     * If the property has @DefaultValue("..."), return a parsed [DefaultPropertyValue], else null.
     */
    private fun KSPropertyDeclaration.extractDefaultPropertyValue(): DefaultPropertyValue? {
        // find annotation
        val ann: KSAnnotation? = annotations.firstOrNull {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == DefaultValue::class.qualifiedName
        }

        // get the String argument
        val raw = ann?.arguments
            ?.firstOrNull { it.name?.asString() == DefaultValue::value.name }
            ?.value as? String

        logger.debug("Raw default value from annotation: '$raw'", tier = 2)

        val packageName = ann?.arguments
            ?.firstOrNull { it.name?.asString() == DefaultValue::packageName.name }
            ?.value
            ?.toString()

        val className = ann?.arguments
            ?.firstOrNull { it.name?.asString() == DefaultValue::className.name }
            ?.value
            ?.toString()

        logger.debug("Class reference: $packageName.$className", tier = 2)

        if (raw == null || packageName == null || className == null) return null

        // decide whether this raw should be a literal or raw code:
        // here we assume it’s raw Kotlin snippet (e.g. "listOf(1,2,3)"); adjust to %S if literal
        val isStringClass = className == "String"
        logger.debug("Is String class: $isStringClass", tier = 2)
        val template = if (isStringClass) "%S" else "%L"
        val cb = CodeBlock.of(template, raw)
        logger.debug("CodeBlock for default value: $cb", tier = 2)

        return DefaultPropertyValue(rawValue = raw, codeBlock = cb, packageName, className)
    }
}
