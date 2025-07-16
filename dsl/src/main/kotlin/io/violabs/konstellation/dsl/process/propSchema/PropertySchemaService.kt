package io.violabs.konstellation.dsl.process.propSchema

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.CodeBlock
import io.violabs.konstellation.dsl.domain.DefaultDomainProperty
import io.violabs.konstellation.dsl.domain.DefaultPropertyValue
import io.violabs.konstellation.dsl.domain.DomainConfig
import io.violabs.konstellation.dsl.domain.DomainProperty
import io.violabs.konstellation.dsl.schema.DslPropSchema
import io.violabs.konstellation.metaDsl.annotation.DefaultValue
import kotlin.reflect.KClass

/**
 * Service to handle the conversion of domain properties into DSL property schemas.
 */
interface PropertySchemaService<FACTORY_ADAPTER : PropertySchemaFactoryAdapter, PROP_ADAPTER : DomainProperty> {
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

                DefaultDomainProperty(
                    i, lastIndex,
                    prop,
                    domainConfig.singleEntryTransformByClassName,
                    defaultValue
                )
            }
            .map(propertySchemaFactory::createPropertySchemaFactoryAdapter)
            .map(propertySchemaFactory::determinePropertySchema)
            .toList()
    }
}

/**
 * If the property has @DefaultValue("..."), return a parsed [DefaultPropertyValue], else null.
 */
fun KSPropertyDeclaration.extractDefaultPropertyValue(): DefaultPropertyValue? {
    // find annotation
    val ann: KSAnnotation? = annotations.firstOrNull {
        it.annotationType.resolve().declaration.qualifiedName?.asString() == DefaultValue::class.qualifiedName
    }

    // get the String argument
    val raw = ann?.arguments
        ?.firstOrNull { it.name?.asString() == DefaultValue::value.name }
        ?.value as? String

    val classRef = ann?.arguments
        ?.firstOrNull { it.name?.asString() ==  DefaultValue::classRef.name }
        ?.value as? KClass<*>

    if (raw == null || classRef == null) return null

    // decide whether this raw should be a literal or raw code:
    // here we assume it’s raw Kotlin snippet (e.g. "listOf(1,2,3)"); adjust to %S if literal
    val cb = CodeBlock.of("%L", raw)

    return DefaultPropertyValue(rawValue = raw, codeBlock = cb, classRef = classRef)
}
