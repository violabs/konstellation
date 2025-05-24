package io.violabs.picard.dslTest.process

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo
import io.violabs.picard.dslTest.TestDomainConfig
import io.violabs.picard.metaDsl.builder.kotlinPoet
import io.violabs.picard.metaDsl.config.BuilderConfig
import io.violabs.picard.metaDsl.config.DomainConfig
import io.violabs.picard.metaDsl.process.DefaultPropertySchemaService
import io.violabs.picard.metaDsl.schema.DslPropSchema

class DefaultTestGenerator(
    val propertySchemaService: DefaultPropertySchemaService = DefaultPropertySchemaService()
) : TestGenerator {
    override fun logId(): String? = "DSL_TEST_GENERATOR"

    init {
        logger.enableDebug()
    }

    override fun generate(
        codeGenerator: CodeGenerator,
        domain: KSClassDeclaration,
        builderConfig: BuilderConfig,
        singleEntryTransformByClassName: Map<String, KSClassDeclaration>
    ) {
        val domainConfig = TestDomainConfig(
            builderConfig,
            singleEntryTransformByClassName,
            domain
        )
        generateFilesForDsl(domainConfig, codeGenerator)
    }


    private fun generateFilesForDsl(
        domainConfig: TestDomainConfig,
        codeGenerator: CodeGenerator
    ) {
        logger.debug("-- generating tests for builder --", tier = 0)

        logger.debug("+++ DOMAIN: ${domainConfig.domainClassName} +++")
        logger.debug("package: ${domainConfig.packageName}", tier = 1, branch = true)
        logger.debug("type: ${domainConfig.typeName}", tier = 1, branch = true)
        logger.debug("builder: ${domainConfig.builderName}", tier = 1, branch = true)

        val schemas = propertySchemaService.getParamsFromDomain(domainConfig)

        val fileContent = generateTestFileContent(domainConfig, schemas)

        val fileSpec = kotlinPoet {
            file {
                className = domainConfig.fileClassName
                types(fileContent)
            }
        }

        fileSpec.writeTo(codeGenerator, domainConfig.dependencies)
        logger.debug("file written: ${domainConfig.fileClassName}", tier = 1)

    }


    private fun generateTestFileContent(domainConfig: TestDomainConfig, schemas: List<DslPropSchema>): TypeSpec =
        kotlinPoet {
            val domainClassName = domainConfig.domainClassName

            type {
                name = domainConfig.testClassName.simpleName
            }
        }
}