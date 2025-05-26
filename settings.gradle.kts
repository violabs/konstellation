pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

//plugins {
//    id("org.jetbrains.kotlinx.kover.aggregation") version "0.9.1"
//}

//kover {
////    enableCoverage()
//    reports {
//        includedProjects.addAll(
//            ":cmd",
//            ":core",
//            ":dsl",
//            ":star-charts:loki"
//        )
//        excludesAnnotatedBy.add("io.violabs.konstellation.common.ExcludeFromCoverage")
////        excludedClasses.add("io.violabs.konstellation.domain.api.view.*")
//        verify {
//            rule("Minimum Line Coverage") {
//                bound {
//                    minValue = 80
//                }
//            }
//        }
//    }
//
//
//}

includeModules(
    "core-test",
    "dsl",
    "meta-dsl",
    "generate-test"
)

class Module(private val moduleName: String) {
    override fun toString(): String = moduleName
}

fun includeModules(vararg modules: Any) {
    val ids = modules.asSequence().flatMap { it.asStrings() }.toList()

    logger.debug("including: {}", ids)
    include(ids)
}

fun Any.asStrings(): Sequence<String> = when (this) {
    is String -> sequenceOf(this)
    is List<*> -> this.asSequence().map { it.toString() }
    else -> sequenceOf(toString())
}

fun String.subModules(vararg subModulesIds: String): List<Module> {
    val subModules = subModulesIds.map { Module("$this:$it") }

    return listOf(Module(this)) + subModules
}