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

/**
 * A DSL builder for KotlinPoet, providing a fluent API to create Kotlin code structures.
 * Available methods include:
 * - [KotlinPoetBuilder.property]
 * - [KotlinPoetBuilder.function]
 * - [KotlinPoetBuilder.functions]
 * - [KotlinPoetBuilder.type]
 * - [KotlinPoetBuilder.typeAlias]
 * - [KotlinPoetBuilder.file]
 * - [KotlinPoetBuilder.nestedClass]
 * - [KotlinPoetBuilder.listTypeOf]
 * - [KotlinPoetBuilder.mutableListOf]
 * - [KotlinPoetBuilder.pairTypeOf]
 * @see [KotlinPoetBuilder]
 */
fun <R> kotlinPoet(block: KotlinPoetBuilder.() -> R): R {
    return KotlinPoetBuilder().block()
}

/**
 * Creates a KotlinPoet TypeName for a map with the specified key and value types.
 * @param key The type of the keys in the map.
 * @param value The type of the values in the map.
 * @param nullable Whether the map can be null.
 * @return A TypeName representing a map with the specified key and value types.
 */
fun kpMapOf(key: TypeName, value: TypeName, nullable: Boolean = true): TypeName =
    MAP.parameterizedBy(key, value).copy(nullable = nullable)

/**
 * Creates a KotlinPoet TypeName for a mutable map with the specified key and value types.
 * @param key The type of the keys in the map.
 * @param value The type of the values in the map.
 * @param nullable Whether the map can be null.
 * @return A TypeName representing a mutable map with the specified key and value types.
 */
fun kpMutableMapOf(key: TypeName, value: TypeName, nullable: Boolean = true): TypeName =
    MUTABLE_MAP.parameterizedBy(key, value).copy(nullable = nullable)

/**
 * Creates a KotlinPoet TypeName for a list with the specified type.
 * @param type The type of the elements in the list.
 * @param nullable Whether the list can be null.
 * @return A TypeName representing a list with the specified type.
 */
fun kpListOf(type: TypeName, nullable: Boolean = true): TypeName =
    LIST.parameterizedBy(type).copy(nullable = nullable)

/**
 * Creates a KotlinPoet TypeName for a mutable list with the specified type.
 * @param type The type of the elements in the mutable list.
 * @param nullable Whether the mutable list can be null.
 * @return A TypeName representing a mutable list with the specified type.
 */
fun kpMutableListOf(type: TypeName, nullable: Boolean = true): TypeName =
    MUTABLE_LIST.parameterizedBy(type).copy(nullable = nullable)

/**
 * A builder interface that allows adding parameters to a KotlinPoet specification.
 * This is used to define parameters for functions, properties, and other KotlinPoet constructs.
 */
class KotlinPoetBuilder : ParamSpecEnabled {
    override var params: MutableList<ParameterSpec> = mutableListOf()

    /**
     * Adds a parameter to the list of parameters.
     * Available accessors:
     * - [DefaultKotlinPoetSpec.name]
     * - [KPPropertySpecBuilder.type]
     * - [KPPropertySpecBuilder.initializer]
     * - [KPPropertySpecBuilder.mutable]
     * - [KPPropertySpecBuilder.initNullValue]
     * - [DefaultKotlinPoetSpec.modifiers]
     *
     * @param block A lambda that configures the property [KPPropertySpecBuilder] -> Unit.
     */
    fun property(
        block: KPPropertySpecBuilder.() -> Unit
    ): PropertySpec {
        return KPPropertySpecBuilder().apply(block).build()
    }

    /**
     * Adds a parameter to the list of parameters.
     * Available accessors:
     * - [KPFunSpecBuilder.funName]
     * - [KPFunSpecBuilder.returns]
     * - [KPFunSpecBuilder.kdoc]
     * - [KPFunSpecBuilder.annotations]
     * - [KPFunSpecBuilder.override]
     * - [KPFunSpecBuilder.statements]
     *
     * @param block A lambda that configures the function [KPFunSpecBuilder] -> Unit.
     */
    fun function(block: KPFunSpecBuilder.() -> Unit): FunSpec {
        return KPFunSpecBuilder().apply(block).build()
    }

    /**
     * Adds a list of functions to the list of parameters.
     * Available accessors:
     * - [KPFunSpecBuilder.Group.items]
     * - [KPFunSpecBuilder.Group.add]
     * - [KPFunSpecBuilder.Group.addForEachIn]
     * - [KPFunSpecBuilder.Group.addForEach]
     *
     * @param block A lambda that configures the function group [KPFunSpecBuilder.Group] -> Unit.
     */
    fun functions(block: KPFunSpecBuilder.Group.() -> Unit): List<FunSpec> {
        return KPFunSpecBuilder.Group().apply(block).items
    }

    /**
     * Creates a KotlinPoet TypeSpec with the specified properties.
     * Available properties:
     * - [KPTypeSpecBuilder.annotation]
     * - [KPTypeSpecBuilder.superInterface]
     * - [KPTypeSpecBuilder.typeVariables]
     * - [KPTypeSpecBuilder.properties]
     * - [KPTypeSpecBuilder.functions]
     * - [KPTypeSpecBuilder.nested]
     *
     * @param block A lambda that configures the type [KPTypeSpecBuilder] -> Unit.
     */
    fun type(block: KPTypeSpecBuilder.() -> Unit): TypeSpec {
        return KPTypeSpecBuilder().apply(block).build()
    }

    /**
     * Creates a KotlinPoet TypeAliasSpec with the specified properties.
     * Available properties:
     * [KPTypeAliasSpecBuilder.name]
     * [KPTypeAliasSpecBuilder.type]
     * [KPTypeAliasSpecBuilder.typeVariables]
     *
     * @param block A lambda that configures the type alias [KPTypeAliasSpecBuilder] -> Unit.
     */
    fun typeAlias(block: KPTypeAliasSpecBuilder.() -> Unit): TypeAliasSpec {
        return KPTypeAliasSpecBuilder().apply(block).build()
    }

    /**
     * Creates a KotlinPoet FileSpec with the specified package name and content.
     * Available properties:
     * - [KPFileSpecBuilder.packageName]
     * - [KPFileSpecBuilder.typeSpecs]
     * - [KPFileSpecBuilder.functions]
     * - [KPFileSpecBuilder.properties]
     * - [KPFileSpecBuilder.typeAliases]
     *
     * @param block A lambda that configures the file [KPFileSpecBuilder] -> Unit.
     */
    fun file(block: KPFileSpecBuilder.() -> Unit): FileSpec {
        return KPFileSpecBuilder().apply(block).build()
    }

    /**
     * Creates a nested class name based on the provided class name and extension name.
     * This method allows for creating nested classes with a specific naming convention.
     * @param extensionName The extension name to append to the class name.
     * @param nestedClassName An optional nested class name to append.
     */
    fun ClassName.nestedClass(
        extensionName: String,
        nestedClassName: String? = null
    ): ClassName = nestedClassName?.let {
        ClassName(this.packageName, simpleName + extensionName, nestedClassName)
    } ?: ClassName(this.packageName, simpleName + extensionName)

    /**
     * Creates a KotlinPoet TypeName for a list with the specified parameter class name.
     * @param parameterClassName The class name of the elements in the list.
     * @param nullable Whether the list can be null.
     * @return A TypeName representing a list with the specified parameter class name.
     */
    fun listTypeOf(
        parameterClassName: ClassName,
        nullable: Boolean = true
    ): TypeName = kpListOf(parameterClassName, nullable = nullable)

    /**
     * Creates a KotlinPoet TypeName for a mutable list with the specified parameter class name.
     * @param parameterClassName The class name of the elements in the mutable list.
     * @return A TypeName representing a mutable list with the specified parameter class name.
     */
    fun mutableListOf(parameterClassName: ClassName): TypeName =
        kpMutableListOf(parameterClassName, nullable = false)

    /**
     * Creates a KotlinPoet TypeName for a pair with the specified first and second types.
     * @param firstType The type of the first element in the pair.
     * @param secondType The type of the second element in the pair.
     * @param nullable Whether the pair can be null.
     * @return A TypeName representing a pair with the specified first and second types.
     */
    fun pairTypeOf(
        firstType: TypeName,
        secondType: TypeName,
        nullable: Boolean = true
    ): TypeName =
        Pair::class
            .asTypeName()
            .parameterizedBy(firstType, secondType)
            .copy(nullable = nullable)
}
