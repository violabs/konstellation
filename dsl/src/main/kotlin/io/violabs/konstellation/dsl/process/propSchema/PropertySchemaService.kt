package io.violabs.konstellation.dsl.process.propSchema

import io.violabs.konstellation.dsl.domain.DefaultDomainProperty
import io.violabs.konstellation.dsl.domain.DomainConfig
import io.violabs.konstellation.dsl.domain.DomainProperty
import io.violabs.konstellation.dsl.schema.DslPropSchema

interface PropertySchemaService<FACTORY_ADAPTER : PropertySchemaFactoryAdapter, PROP_ADAPTER : DomainProperty> {
    fun getParamsFromDomain(domainConfig: DomainConfig): List<DslPropSchema>
}

class DefaultPropertySchemaService(
    private val propertySchemaFactory: DefaultPropertySchemaFactory = DefaultPropertySchemaFactory()
) : PropertySchemaService<DefaultPropertySchemaFactoryAdapter, DefaultDomainProperty> {
    override fun getParamsFromDomain(domainConfig: DomainConfig): List<DslPropSchema> {
        val domain = domainConfig.domain
        val lastIndex = domain.getAllProperties().count() - 1

        return domain
            .getAllProperties()
            .mapIndexed { i, prop ->
                DefaultDomainProperty(
                    i, lastIndex,
                    prop,
                    domainConfig.singleEntryTransformByClassName
                )
            }
            .map(propertySchemaFactory::createPropertySchemaFactoryAdapter)
            .map(propertySchemaFactory::determinePropertySchema)
            .toList()
    }
}