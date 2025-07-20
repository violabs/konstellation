package io.violabs.konstellation.dsl.builder

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeSpec

/**
 * A DSL builder for creating KotlinPoet FileSpec objects.
 * This builder allows you to define the class name, imports, type aliases, types, and functions
 * that will be included in the generated Kotlin file.
 */
@Suppress("TooManyFunctions")
class KPFileSpecBuilder {
    var className: ClassName? = null
    private var imports = mutableListOf<Pair<String, String>>()
    private var typeAliases = mutableListOf<TypeAliasSpec>()
    private var types = mutableListOf<TypeSpec>()
    private var functions = mutableListOf<FunSpec>()

    /**
     * Adds a list of [TypeSpec] objects to the file.
     * Available accessors:
     * - [KPTypeSpecBuilder.Group.items]
     * - [KPTypeSpecBuilder.Group.addType]
     */
    fun types(block: KPTypeSpecBuilder.Group.() -> Unit) {
        types = KPTypeSpecBuilder.Group().apply(block).items
    }

    /**
     * Adds a list of [TypeAliasSpec] objects to the file.
     * Available accessors:
     * - [KPFunSpecBuilder.Group.items]
     * - [KPFunSpecBuilder.Group.addTypeAlias]
     */
    fun functions(block: KPFunSpecBuilder.Group.() -> Unit) {
        functions = KPFunSpecBuilder.Group().apply(block).items
    }

    /**
     * Adds a list of [FunSpec] objects to the file.
     *
     * @param funs A mutable list of [FunSpec] to be added to the file.
     */
    fun functions(funs: MutableList<FunSpec>) { this.functions = funs }

    /**
     * Adds a list of [TypeAliasSpec] objects to the file.
     *
     * @param aliases A mutable list of [TypeAliasSpec] to be added to the file.
     */
    fun typeAliases(vararg aliases: TypeAliasSpec) {
        this.typeAliases = aliases.toMutableList()
    }

    /**
     * Adds a list of [TypeAliasSpec] objects to the file.
     *
     * @param specs A mutable list of [TypeAliasSpec] to be added to the file.
     */
    fun types(vararg specs: TypeSpec) {
        this.types = specs.toMutableList()
    }

    /**
     * Adds import to the file.
     *
     * @param className The [ClassName] to be imported.
     */
    fun addImport(className: ClassName) {
        imports.add(className.packageName to className.simpleName)
    }

    fun addImport(className: String) {
        imports.add(className.substringBeforeLast(".") to className.substringAfterLast('.'))
    }

    /**
     * Adds an import to the file.
     *
     * @param classNamePair A pair of package name and simple name to be imported.
     */
    fun addImport(classNamePair: Pair<String, String>) {
        imports.add(classNamePair)
    }

    /**
     * Adds an import to the file.
     *
     * @param packageName The package name of the import.
     * @param methodName The simple name of the import.
     */
    fun addImport(packageName: String, methodName: String) {
        imports.add(packageName to methodName)
    }

    /**
     * Adds an import to the file if the condition is true.
     *
     * @param condition The condition to check before adding the import.
     * @param packageName The package name of the import.
     * @param simpleName The simple name of the import.
     */
    fun addImportIf(condition: Boolean, packageName: String, simpleName: String) =
        if (condition) addImport(packageName, simpleName) else Unit

    /**
     * Builds the [FileSpec] with the specified class name, imports, type aliases, types, and functions.
     */
    fun build(): FileSpec {
        val className = requireNotNull(className) { "File - Class name must be set" }
        var spec: FileSpec.Builder = FileSpec
            .builder(className)
            .indent("    ")

        for (alias in typeAliases) {
            spec = spec.addTypeAlias(alias)
        }

        for (type in types) {
            spec = spec.addType(type)
        }

        for (function in functions) {
            spec = spec.addFunction(function)
        }

        for (import in imports) {
            spec = spec.addImport(import.first, import.second)
        }

        return spec.build()
    }
}
