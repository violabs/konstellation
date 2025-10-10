package io.violabs.konstellation.dsl.schema.model

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import io.violabs.konstellation.dsl.builder.kotlinPoet
import io.violabs.konstellation.dsl.builder.kpListOf
import io.violabs.konstellation.dsl.builder.kpMapOf
import io.violabs.konstellation.dsl.domain.DefaultPropertyValue
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

sealed class DslProp {
    abstract val name: String
    abstract val nullableAssignment: Boolean
    open val kdoc: String? = null

    data class Default(
        override val name: String,
        val actualType: TypeName,
        override val nullableAssignment: Boolean = true,
        val defaultValue: DefaultPropertyValue? = null,
        override val kdoc: String? = null,
        val accessModifier: KModifier = KModifier.PROTECTED
    ) : DslProp()

    data class BooleanProp(
        override val name: String,
        override val nullableAssignment: Boolean = true,
        val defaultValue: DefaultPropertyValue? = null,
        override val kdoc: String? = null
    ) : DslProp()

    data class ListProp(
        override val name: String,
        val elementType: TypeName,
        override val nullableAssignment: Boolean = true,
        override val kdoc: String? = null
    ) : DslProp()

    data class MapProp(
        override val name: String,
        val keyType: TypeName,
        val valueType: TypeName,
        override val nullableAssignment: Boolean = true,
        override val kdoc: String? = null
    ) : DslProp()

    data class GroupProp(
        override val name: String,
        val builtClassName: ClassName,
        override val nullableAssignment: Boolean = true,
        override val kdoc: String? = null
    ) : DslProp()

    data class MapGroupProp(
        override val name: String,
        val keyType: TypeName,
        val valueType: TypeName,
        override val nullableAssignment: Boolean = true,
        override val kdoc: String? = null
    ) : DslProp()

    data class BuilderProp(
        override val name: String,
        val originalType: TypeName,
        val nestedBuilderClassName: ClassName,
        override val nullableAssignment: Boolean = true,
        override val kdoc: String? = null
    ) : DslProp()
}

object DslPropRenderer {
    fun toPropertySpec(prop: DslProp): PropertySpec = when (prop) {
        is DslProp.Default -> defaultProperty(prop)
        is DslProp.BooleanProp -> defaultProperty(
            DslProp.Default(
                prop.name,
                propTypeBoolean(prop.nullableAssignment),
                prop.nullableAssignment,
                prop.defaultValue,
                prop.kdoc
            )
        )
        is DslProp.ListProp -> defaultProperty(
            DslProp.Default(
                prop.name,
                kpListOf(prop.elementType, nullable = true),
                prop.nullableAssignment,
                null,
                prop.kdoc
            )
        )
        is DslProp.MapProp -> defaultProperty(
            DslProp.Default(
                prop.name,
                kpMapOf(prop.keyType, prop.valueType, nullable = true),
                prop.nullableAssignment,
                null,
                prop.kdoc
            )
        )
        is DslProp.GroupProp -> defaultProperty(
            DslProp.Default(
                prop.name,
                kpListOf(prop.builtClassName, nullable = true),
                prop.nullableAssignment,
                null,
                prop.kdoc
            )
        )
        is DslProp.MapGroupProp -> defaultProperty(
            DslProp.Default(
                prop.name,
                kpMapOf(prop.keyType, prop.valueType, nullable = true),
                prop.nullableAssignment,
                null,
                prop.kdoc
            )
        )
        is DslProp.BuilderProp -> defaultProperty(
            DslProp.Default(
                prop.name,
                prop.originalType,
                prop.nullableAssignment,
                null,
                prop.kdoc
            )
        )
    }

    fun accessors(prop: DslProp): List<FunSpec> = when (prop) {
        is DslProp.Default -> emptyList()
        is DslProp.BooleanProp -> booleanAccessors(prop)
        is DslProp.ListProp -> listAccessors(prop)
        is DslProp.MapProp -> mapAccessors(prop)
        is DslProp.GroupProp -> groupAccessors(prop)
        is DslProp.MapGroupProp -> mapGroupAccessors(prop)
        is DslProp.BuilderProp -> builderAccessors(prop)
    }

    private fun booleanAccessors(prop: DslProp.BooleanProp): List<FunSpec> = kotlinPoet {
        functions {
            add {
                funName = prop.name
                prop.kdoc?.let { kdoc(it) }
                val p = param { booleanType(); defaultValue(prop.defaultValue?.rawValue?.toBoolean() ?: true) }
                statements { addLine("this.%N = %N", prop.name, p) }
            }
        }
    }

    private fun listAccessors(prop: DslProp.ListProp): List<FunSpec> = kotlinPoet {
        functions {
            add {
                funName = prop.name
                prop.kdoc?.let { kdoc(it) }
                varargParam { type(prop.elementType, nullable = false) }
                statements { addLine("this.%N = items.toList()", prop.name) }
            }
        }
    }

    private fun mapAccessors(prop: DslProp.MapProp): List<FunSpec> = kotlinPoet {
        functions {
            add {
                funName = prop.name
                prop.kdoc?.let { kdoc(it) }
                val pairType = pairTypeOf(prop.keyType, prop.valueType, nullable = false)
                varargParam { type(pairType) }
                statements { addLine("this.%N = items.toMap()", prop.name) }
            }
        }
    }

    private fun groupAccessors(prop: DslProp.GroupProp): List<FunSpec> = kotlinPoet {
        val receiverName = prop.builtClassName.nestedClass("DslBuilder", "Group")
        functions {
            add {
                funName = prop.name
                prop.kdoc?.let { kdoc(it) }
                param { lambdaType { receiver = receiverName } }
                statements { addLine("this.%N = %T().apply(block).items()", prop.name, receiverName) }
            }
        }
    }

    private fun mapGroupAccessors(prop: DslProp.MapGroupProp): List<FunSpec> = kotlinPoet {
        val valueClassName = (prop.valueType.copy(nullable = false) as ClassName)
        val mapGroupClass = ClassName(
            valueClassName.packageName,
            valueClassName.simpleName + "DslBuilder",
            "MapGroup"
        ).parameterizedBy(prop.keyType.copy(nullable = false))

        functions {
            add {
                funName = prop.name
                prop.kdoc?.let { kdoc(it) }
                param { lambdaType { receiver = mapGroupClass } }
                statements { addLine("this.%N = %T().apply(block).items().toMap()", prop.name, mapGroupClass) }
            }
        }
    }

    private fun builderAccessors(prop: DslProp.BuilderProp): List<FunSpec> = kotlinPoet {
        functions {
            add {
                funName = prop.name
                prop.kdoc?.let { kdoc(it) }
                param { lambdaType { receiver = prop.nestedBuilderClassName } }
                statements {
                    addLine("val builder = %T()", prop.nestedBuilderClassName)
                    addLine("builder.block()")
                    addLine("this.%N = builder.build()", prop.name)
                }
            }
        }
    }

    private fun defaultProperty(def: DslProp.Default): PropertySpec = kotlinPoet {
        property {
            when (def.accessModifier) {
                KModifier.PUBLIC -> public()
                KModifier.PROTECTED -> protected()
                else -> {}
            }
            variable()
            name = def.name
            type(def.actualType.copy(nullable = true))
            def.defaultValue?.codeBlock?.let { initializer = it } ?: initNullValue()
        }
    }

    // Helper to get a TypeName for Boolean honoring nullability; callers use as type for property only.
    private fun propTypeBoolean(nullable: Boolean): TypeName = com.squareup.kotlinpoet.BOOLEAN.copy(nullable = nullable)
}
