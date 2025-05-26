package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.MUTABLE_LIST
import com.squareup.kotlinpoet.MUTABLE_MAP
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

fun <R> kotlinPoet(block: KotlinPoetBuilder.() -> R): R {
    return KotlinPoetBuilder().block()
}

fun kpMapOf(key: TypeName, value: TypeName, nullable: Boolean = true): TypeName =
    MAP.parameterizedBy(key, value).copy(nullable = nullable)

fun kpMutableMapOf(key: TypeName, value: TypeName, nullable: Boolean = true): TypeName =
    MUTABLE_MAP.parameterizedBy(key, value).copy(nullable = nullable)

fun kpListOf(type: TypeName, nullable: Boolean = true): TypeName =
    LIST.parameterizedBy(type).copy(nullable = nullable)

fun kpMutableListOf(type: TypeName, nullable: Boolean = true): TypeName =
    MUTABLE_LIST.parameterizedBy(type).copy(nullable = nullable)

class KotlinPoetBuilder : ParamSpecEnabled {
    override var params: MutableList<ParameterSpec> = mutableListOf()

    fun ClassName.nestedClass(
        extensionName: String,
        nestedClassName: String? = null
    ): ClassName = nestedClassName?.let {
        ClassName(this.packageName, simpleName + extensionName, nestedClassName)
    } ?: ClassName(this.packageName, simpleName + extensionName)

    fun listTypeOf(
        parameterClassName: ClassName,
        nullable: Boolean = true
    ): TypeName = kpListOf(parameterClassName, nullable = nullable)

    fun mutableListOf(parameterClassName: ClassName): TypeName =
        kpMutableListOf(parameterClassName, nullable = false)

    fun pairTypeOf(
        firstType: TypeName,
        secondType: TypeName,
        nullable: Boolean = true
    ): TypeName =
        Pair::class
            .asTypeName()
            .parameterizedBy(firstType, secondType)
            .copy(nullable = nullable)

    fun property(
        block: KPPropertySpecBuilder.() -> Unit
    ): PropertySpec {
        return KPPropertySpecBuilder().apply(block).build()
    }

    fun function(block: KPFunSpecBuilder.() -> Unit): FunSpec {
        return KPFunSpecBuilder().apply(block).build()
    }

    fun functions(block: KPFunSpecBuilder.Group.() -> Unit): List<FunSpec> {
        return KPFunSpecBuilder.Group().apply(block).items
    }

    /**
     * Available properties:
     * - [KPTypeSpecBuilder.annotation]
     * - [KPTypeSpecBuilder.superInterface]
     * - [KPTypeSpecBuilder.typeVariables]
     * - [KPTypeSpecBuilder.properties]
     * - [KPTypeSpecBuilder.functions]
     * - [KPTypeSpecBuilder.nested]
     */
    fun type(block: KPTypeSpecBuilder.() -> Unit): TypeSpec {
        return KPTypeSpecBuilder().apply(block).build()
    }

    /**
     * Available properties:
     * [KPTypeAliasSpecBuilder.name]
     * [KPTypeAliasSpecBuilder.type]
     * [KPTypeAliasSpecBuilder.typeVariables]
     */
    fun typeAlias(block: KPTypeAliasSpecBuilder.() -> Unit): TypeAliasSpec {
        return KPTypeAliasSpecBuilder().apply(block).build()
    }

    fun file(block: KPFileSpecBuilder.() -> Unit): FileSpec {
        return KPFileSpecBuilder().apply(block).build()
    }
}