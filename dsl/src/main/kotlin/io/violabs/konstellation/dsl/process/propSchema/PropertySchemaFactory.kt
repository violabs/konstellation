package io.violabs.konstellation.dsl.process.propSchema

import com.squareup.kotlinpoet.*
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
        logger.debug(propertyAdapter.simpleName(), tier = 2, branch = branch, continuous = true)

        val type = propertyAdapter.type
        logger.debug("type:  $type", tier = 3, branch = branch, continuous = true)

        val singleEntryTransform = propertyAdapter.singleEntryTransformString()
        logger.debug("singleEntryTransform: $singleEntryTransform", tier = 3, branch = branch, continuous = true)
    }

    /**
     * Resolve the correct [DslPropSchema] implementation for the provided adapter.
     *
     * @param adapter the property adapter being processed
     * @param isLast whether this is the last parameter being generated
     * @param log enable debug logging for this invocation
     */
    fun determinePropertySchema(
        adapter: T,
        isLast: Boolean = false,
        log: Boolean = true
    ): DslPropSchema
}

class DefaultPropertySchemaFactory() :
    AbstractPropertySchemaFactory<DefaultPropertySchemaFactoryAdapter, DefaultDomainProperty>() {
    init {
        logger.enableDebug()
    }

    override fun createPropertySchemaFactoryAdapter(propertyAdapter: DefaultDomainProperty): DefaultPropertySchemaFactoryAdapter {
        logAdapter(propertyAdapter)

        return DefaultPropertySchemaFactoryAdapter(propertyAdapter)
    }
}


/**
 * Base implementation of [PropertySchemaFactory] with common resolution logic.
 */
abstract class AbstractPropertySchemaFactory<T : PropertySchemaFactoryAdapter, P : DomainProperty> : PropertySchemaFactory<T, P> {
    /** @inheritdoc */
    override fun determinePropertySchema(adapter: T, isLast: Boolean, log: Boolean): DslPropSchema {
        val logger = logger.copy(isDebugEnabled = log)
        val propName = adapter.propName
        val actualPropertyType: TypeName = adapter.actualPropTypeName
        val isNullable = actualPropertyType.isNullable
        val nonNullPropType = adapter.nonNullablePropTypeName()

        val branch = !isLast

        logger.debug("mapping '$propName'", tier = 3, branch = branch, continuous = true)
        logger.debug("nullable: $isNullable", tier = 4, branch = branch, continuous = true)

        if (adapter.hasSingleEntryTransform) {
            return buildSingleTransformProp(adapter, log, branch)
        }

        val propertyNonNullableClassName: ClassName? = adapter.propertyNonNullableClassName
        val hasGeneratedDSLAnnotation = adapter.hasGeneratedDslAnnotation

        if (hasGeneratedDSLAnnotation && propertyNonNullableClassName != null) {
            logger.debug("BuilderProp", tier = 4, branch = branch, continuous = true)
            return createBuilderProp(adapter)
        }

        return when {
            BOOLEAN == nonNullPropType -> {
                logger.debug("BooleanProp", tier = 4, branch = branch, continuous = true)
                BooleanPropSchema(propName, isNullable)
            }

            DEFAULT_TYPE_NAMES.contains(nonNullPropType) -> {
                logger.debug("DefaultProp", tier = 4, branch = branch, continuous = true)
                DefaultPropSchema(propName, actualPropertyType, isNullable)
            }

            checkCollectionType(adapter, MAP, Map::class) -> {
                logger.debug("[CHOICE] map branch", tier = 4, branch = branch, continuous = true)
                val mapGroupType: GeneratedDsl.MapGroupType? = adapter.mapDetails()?.mapGroupType
                if (mapGroupType in GeneratedDsl.MapGroupType.ACTIVE_TYPES) {
                    logger.debug("[DECISION] build MapGroupProp", tier = 4, branch = branch, continuous = true)
                    createMapGroupProp(adapter)
                } else {
                    logger.debug("[DECISION] build MapProp", tier = 4, branch = branch, continuous = true)
                    createMapProp(adapter)
                }
            }

            checkCollectionType(adapter, LIST, List::class) -> {
                logger.debug("[CHOICE] list branch", tier = 4, branch = branch, continuous = true)
                if (adapter.isGroupElement) {
                    logger.debug("[DECISION] build GroupProp", tier = 4, branch = branch, continuous = true)
                    createGroupProp(adapter)
                } else {
                    logger.debug("[DECISION] build ListProp", tier = 4, branch = branch, continuous = true)
                    createListProp(adapter)
                }
            }

            else -> {
                logger.warn("Property '$propName' of type '${actualPropertyType}' could not be mapped to a known DSLParam type. Using DefaultParam as a fallback.")
                val param = DefaultPropSchema(propName, actualPropertyType, isNullable)
                logger.debug("-> DefaultProp (fallback)", tier = 4, branch = branch, continuous = true)
                param
            }
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
        log: Boolean = true,
        branch: Boolean = true
    ): DslPropSchema {
        val transformType = adapter.transformType

        logger.debug("SingleEntryTransform", tier = 4, branch = branch, continuous = true)
        logger.debug("template: ${adapter.transformTemplate}", tier = 5, branch = branch, continuous = true)
        logger.debug("type: $transformType", tier = 5, branch = branch, continuous = true)

        if (transformType == null) {
            logger.warn("SingleEntryTransformDSL.inputType is missing or not a KSType.")
            return DefaultPropSchema(adapter)
        }

        if (log) logger.debug("-> SingleTransformProp", tier = 4, branch = branch, continuous = true)
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
        logger.debug("nestedBuilder: $nestedBuilderClassName", tier = 5, continuous = true)
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
        logger.debug("listElementClassName: $groupElementClassName", tier = 5, continuous = true)
        val builderClassName = ClassName(groupElementClassName.packageName, groupElementClassName.simpleName + "DslBuilder")
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
        val props = declaration?.getAllProperties()?.map { it.simpleName.asString() }?.toList() ?: return null

        if (props.isEmpty()) return null
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
            logger.debug("mapElementKey: $keyType", tier = 5, continuous = true)
            logger.debug("mapElementValue: $valueType", tier = 5, continuous = true)
            return MapPropSchema(propName, keyType, valueType, adapter.hasNullableAssignment)
        }

        logger.warn(
            "Attempted to create MapProp for unsupported type '$actualPropertyType'. Falling back to DefaultProp."
        )
        return DefaultPropSchema(propName, actualPropertyType, adapter.hasNullableAssignment)
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
            logger.debug("listElementType: $elementTypeArgument", tier = 5, continuous = true)
            return ListPropSchema(propName, elementTypeArgument, adapter.hasNullableAssignment)
        }

        logger.warn(
            "Attempted to create ListProp for unsupported type '$actualPropertyType'. Falling back to DefaultProp."
        )
        return DefaultPropSchema(propName, actualPropertyType, adapter.hasNullableAssignment)
    }
}