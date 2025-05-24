package io.violabs.picard.dslTest

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import io.violabs.picard.metaDsl.config.BuilderConfig
import io.violabs.picard.metaDsl.config.DomainConfig

class TestDomainConfig(
    builderConfig: BuilderConfig,
    singleEntryTransformByClassName: Map<String, KSClassDeclaration>,
    domain: KSClassDeclaration,
) : DomainConfig(
    builderConfig,
    singleEntryTransformByClassName,
    domain
) {
    val testClassName = ClassName(packageName, "${builderClassName.simpleName}Test")
    override val fileClassName: ClassName = testClassName
}