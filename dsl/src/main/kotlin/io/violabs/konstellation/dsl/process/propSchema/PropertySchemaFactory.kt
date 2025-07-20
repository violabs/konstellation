package io.violabs.konstellation.dsl.process.propSchema

import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.violabs.konstellation.metaDsl.annotation.GeneratedDsl
import io.violabs.konstellation.dsl.domain.DefaultDomainProperty
import io.violabs.konstellation.dsl.domain.DomainProperty
import io.violabs.konstellation.dsl.schema.BooleanPropSchema
import io.violabs.konstellation.dsl.schema.BuilderPropSchema
import io.violabs.konstellation.dsl.schema.DefaultPropSchema
import io.violabs.konstellation.dsl.schema.DslPropSchema
import io.violabs.konstellation.dsl.schema.GroupPropSchema
import io.violabs.konstellation.dsl.schema.ListPropSchema
import io.violabs.konstellation.dsl.schema.MapGroupPropSchema
import io.violabs.konstellation.dsl.schema.MapPropSchema
import io.violabs.konstellation.dsl.schema.SingleTransformPropSchema
import io.violabs.konstellation.dsl.utils.VLoggable
import kotlin.collections.contains
import kotlin.reflect.KClass


private val DEFAULT_TYPE_NAMES = listOf(
    CHAR, STRING, BYTE, SHORT, INT, LONG, DOUBLE, FLOAT
)

/**
 * Responsible for creating [DslPropSchema] instances for a given property adapter.
 */
interface PropertySchemaFactory<T : PropertySchemaFactoryAdapter, P : DomainProperty> : VLoggable {
    /** logger used for debug output */
//    val logger: Logger
    override fun logId(): String? = PropertySchemaFactory::class.simpleName

    fun createPropertySchemaFactoryAdapter(propertyAdapter: P): T

    fun logAdapter(propertyAdapter: P) {
        val branch = propertyAdapter.continueBranch()
        logger.debug(propertyAdapter.simpleName(), tier = 2, branch = branch)

        val type = propertyAdapter.type
        logger.debug("type:  $type", tier = 3, branch = branch)

        val singleEntryTransform = propertyAdapter.singleEntryTransformString()
        logger.debug("singleEntryTransform: $singleEntryTransform", tier = 3, branch = branch)
    }

    /**
     * Resolve the correct [DslPropSchema] implementation for the provided adapter.
     *
     * @param adapter the property adapter being processed
     * @param isLast whether this is the last parameter being generated
     */
    fun determinePropertySchema(
        adapter: T,
        isLast: Boolean = false
    ): DslPropSchema
}

/**
 * Default implementation of [PropertySchemaFactory] for [DefaultDomainProperty].
 * This factory creates property schema adapters for the default domain properties.
 */
class DefaultPropertySchemaFactory :
    AbstractPropertySchemaFactory<DefaultPropertySchemaFactoryAdapter, DefaultDomainProperty>() {

    override fun createPropertySchemaFactoryAdapter(
        propertyAdapter: DefaultDomainProperty
    ): DefaultPropertySchemaFactoryAdapter {
        logAdapter(propertyAdapter)

        return DefaultPropertySchemaFactoryAdapter(propertyAdapter)
    }
}


/**
 * Base implementation of [PropertySchemaFactory] with common resolution logic.
 */
abstract class AbstractPropertySchemaFactory<T : PropertySchemaFactoryAdapter, P : DomainProperty> :
    PropertySchemaFactory<T, P> {
    override fun determinePropertySchema(adapter: T, isLast: Boolean): DslPropSchema {
        val propName = adapter.propName
        val actualPropertyType: TypeName = adapter.actualPropTypeName
        val isNullable = actualPropertyType.isNullable
        val nonNullPropType = adapter.nonNullablePropTypeName()

        val branch = !isLast

        logger.debug("mapping '$propName'", tier = 3, branch = branch)
        logger.debug("nullable: $isNullable", tier = 4, branch = branch)

        return getAnnotated(adapter, branch) ?: when {
            BOOLEAN == nonNullPropType -> {
                logger.debug("BooleanProp", tier = 4, branch = branch)
                BooleanPropSchema(propName, isNullable, adapter.defaultValue)
            }

            DEFAULT_TYPE_NAMES.contains(nonNullPropType) -> {
                logger.debug("DefaultProp", tier = 4, branch = branch)
                DefaultPropSchema(propName, actualPropertyType, isNullable, adapter.defaultValue)
            }

            checkCollectionType(adapter, MAP, Map::class) -> {
                logger.debug("[CHOICE] map branch", tier = 4, branch = branch)
                val mapGroupType: GeneratedDsl.MapGroupType? = adapter.mapDetails()?.mapGroupType
                if (mapGroupType in GeneratedDsl.MapGroupType.ACTIVE_TYPES) {
                    logger.debug("[DECISION] build MapGroupProp", tier = 4, branch = branch)
                    createMapGroupProp(adapter)
                } else {
                    logger.debug("[DECISION] build MapProp", tier = 4, branch = branch)
                    createMapProp(adapter)
                }
            }

            checkCollectionType(adapter, LIST, List::class) -> {
                logger.debug("[CHOICE] list branch", tier = 4, branch = branch)
                if (adapter.isGroupElement) {
                    logger.debug("[DECISION] build GroupProp", tier = 4, branch = branch)
                    createGroupProp(adapter)
                } else {
                    logger.debug("[DECISION] build ListProp", tier = 4, branch = branch)
                    createListProp(adapter)
                }
            }

            else -> {
                logger.warn("Property '$propName' of type '${actualPropertyType}' " +
                    "could not be mapped to a known DSLParam type. Using DefaultParam as a fallback.")
                val param = DefaultPropSchema(propName, actualPropertyType, isNullable, adapter.defaultValue)
                logger.debug("-> DefaultProp (fallback)", tier = 4, branch = branch)
                param
            }
        }
    }

    private fun getAnnotated(adapter: T, branch: Boolean): DslPropSchema? {
        if (adapter.hasSingleEntryTransform) {
            return buildSingleTransformProp(adapter, branch)
        }

        val propertyNonNullableClassName: ClassName? = adapter.propertyNonNullableClassName
        val hasGeneratedDSLAnnotation = adapter.hasGeneratedDslAnnotation

        return if (hasGeneratedDSLAnnotation && propertyNonNullableClassName != null) {
            logger.debug("BuilderProp", tier = 4, branch = branch)
            createBuilderProp(adapter)
        } else {
            null
        }
    }

    private fun checkCollectionType(
        adapter: T,
        expectedType: TypeName,
        expectedClass: KClass<*>
    ): Boolean {
        val nonNullPropType = adapter.nonNullablePropTypeName()
        val isRawCollection = nonNullPropType is ParameterizedTypeName && nonNullPropType.rawType == expectedType
        val isQualifiedCollection = adapter.propertyClassDeclarationQualifiedName == expectedClass.qualifiedName

        return isRawCollection || isQualifiedCollection
    }

    private fun buildSingleTransformProp(
        adapter: PropertySchemaFactoryAdapter,
        branch: Boolean = true
    ): DslPropSchema {
        val transformType = adapter.transformType

        logger.debug("SingleEntryTransform", tier = 4, branch = branch)
        logger.debug("template: ${adapter.transformTemplate}", tier = 5, branch = branch)
        logger.debug("type: $transformType", tier = 5, branch = branch)

        if (transformType == null) {
            logger.warn("SingleEntryTransformDSL.inputType is missing or not a KSType.")
            return DefaultPropSchema(adapter)
        }

        logger.debug("-> SingleTransformProp", tier = 4, branch = branch)
        return SingleTransformPropSchema(adapter)
    }

    private fun createBuilderProp(
        adapter: T,
    ): BuilderPropSchema {
        val propertyNonNullableClassName = requireNotNull(adapter.propertyNonNullableClassName) {
            "Could not determine property non-nullable class name."
        }
        val nestedBuilderName = propertyNonNullableClassName.simpleName + "DslBuilder"
        val nestedBuilderClassName = ClassName(propertyNonNullableClassName.packageName, nestedBuilderName)
        logger.debug("nestedBuilder: $nestedBuilderClassName", tier = 5)
        val kdoc = builderDoc(nestedBuilderClassName, adapter.propertyClassDeclaration)
        
        return BuilderPropSchema(
            adapter.propName,
            adapter.actualPropTypeName,
            nestedBuilderClassName,
            adapter.hasNullableAssignment,
            kdoc = kdoc
        )
    }

    private fun createGroupProp(adapter: T): GroupPropSchema {
        val groupElementClassName = requireNotNull(adapter.groupElementClassName) {
            "Could not determine group element class name."
        }
        logger.debug("listElementClassName: $groupElementClassName", tier = 5)
        val builderClassName = ClassName(
            groupElementClassName.packageName,
            groupElementClassName.simpleName + "DslBuilder"
        )
        val kdoc = builderDoc(builderClassName, adapter.groupElementClassDeclaration)
        return GroupPropSchema(
            adapter.propName,
            adapter.actualPropTypeName,
            groupElementClassName,
            adapter.hasNullableAssignment,
            kdoc = kdoc
        )
    }

    private fun createMapGroupProp(adapter: T): MapGroupPropSchema {
        val mapDetails = requireNotNull(adapter.mapDetails) { "Please add map details to the map parameter" }
        val kdoc = builderDoc(mapDetails.valueClass(), adapter.mapValueClassDeclaration)

        return MapGroupPropSchema(
            adapter.propName,
            mapDetails.keyType,
            mapDetails.valueType,
            adapter.hasNullableAssignment,
            kdoc = kdoc
        )
    }

    private fun builderDoc(builderClass: ClassName, declaration: KSClassDeclaration?): String? {
        val props = declaration
            ?.getAllProperties()
            ?.map { it.simpleName.asString() }
            ?.toList()
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        val list = props.sorted().joinToString("\n") { "* [${builderClass.simpleName}.$it]" }
        return "Available builder functions:\n$list"
    }

    /**
     * Build a [MapPropSchema] from the adapter when the property is a Map type.
     * Falls back to [DefaultPropSchema] when the type information is insufficient.
     */
    private fun createMapProp(adapter: T): DslPropSchema {
        val propName = adapter.propName
        val actualPropertyType: TypeName = adapter.actualPropTypeName

        if (actualPropertyType is ParameterizedTypeName && actualPropertyType.rawType == MAP) {
            val keyType: TypeName = actualPropertyType.typeArguments.first()
            val valueType: TypeName = actualPropertyType.typeArguments.last()
            logger.debug("mapElementKey: $keyType", tier = 5)
            logger.debug("mapElementValue: $valueType", tier = 5)
            return MapPropSchema(propName, keyType, valueType, adapter.hasNullableAssignment)
        }

        logger.warn(
            "Attempted to create MapProp for unsupported type '$actualPropertyType'. Falling back to DefaultProp."
        )
        return DefaultPropSchema(propName, actualPropertyType, adapter.hasNullableAssignment, adapter.defaultValue)
    }

    /**
     * Build a [ListPropSchema] from the adapter when the property is a List type.
     * Falls back to [DefaultPropSchema] when the type information is insufficient.
     */
    private fun createListProp(adapter: T): DslPropSchema {
        val propName = adapter.propName
        val actualPropertyType: TypeName = adapter.actualPropTypeName

        if (actualPropertyType is ParameterizedTypeName && actualPropertyType.rawType == LIST) {
            val elementTypeArgument: TypeName = actualPropertyType.typeArguments.first()
            logger.debug("listElementType: $elementTypeArgument", tier = 5)
            return ListPropSchema(propName, elementTypeArgument, adapter.hasNullableAssignment)
        }

        logger.warn(
            "Attempted to create ListProp for unsupported type '$actualPropertyType'. Falling back to DefaultProp."
        )
        return DefaultPropSchema(propName, actualPropertyType, adapter.hasNullableAssignment, adapter.defaultValue)
    }
}
