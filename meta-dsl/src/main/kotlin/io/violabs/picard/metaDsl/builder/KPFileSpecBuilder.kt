package io.violabs.picard.metaDsl.builder

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

class KPFileSpecBuilder {
    var className: ClassName? = null
    private var imports = mutableListOf<Pair<String, String>>()
    private var types = mutableListOf<TypeSpec>()
    private var functions = mutableListOf<FunSpec>()

    fun types(block: KPTypeSpecBuilder.Group.() -> Unit) {
        types = KPTypeSpecBuilder.Group().apply(block).items
    }

    fun functions(block: KPFunSpecBuilder.Group.() -> Unit) {
        functions = KPFunSpecBuilder.Group().apply(block).items
    }

    fun functions(funs: MutableList<FunSpec>) { this.functions = funs }

    fun types(vararg specs: TypeSpec) {
        this.types = specs.toMutableList()
    }

    fun addImport(className: ClassName) {
        imports.add(className.packageName to className.simpleName)
    }

    fun addImport(classNamePair: Pair<String, String>) {
        imports.add(classNamePair)
    }

    fun addImport(packageName: String, methodName: String) {
        imports.add(packageName to methodName)
    }

    fun addImportIf(condition: Boolean, packageName: String, simpleName: String) =
        if (condition) addImport(packageName, simpleName) else Unit

    fun build(): FileSpec {
        val className = requireNotNull(className) { "File - Class name must be set" }
        var spec = FileSpec
            .builder(className)
            .indent("    ")

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