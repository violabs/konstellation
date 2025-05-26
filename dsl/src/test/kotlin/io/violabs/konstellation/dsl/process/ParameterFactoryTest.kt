package io.violabs.konstellation.dsl.process

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.violabs.geordi.UnitSim
import io.violabs.konstellation.dsl.domain.DomainProperty
import io.violabs.konstellation.dsl.process.propSchema.AbstractPropertySchemaFactory
import io.violabs.konstellation.dsl.process.propSchema.PropertySchemaFactoryAdapter
import io.violabs.konstellation.metaDsl.annotation.GeneratedDsl
import org.junit.jupiter.api.Test

class ParameterFactoryTest : UnitSim() {
    val parameterFactory = object : AbstractPropertySchemaFactory<TestParamFactoryAdaptor, TestPropDomain>() {
        override fun createPropertySchemaFactoryAdapter(propertyAdapter: TestPropDomain): TestParamFactoryAdaptor {
            return TestParamFactoryAdaptor(propertyAdapter.type, propertyAdapter.isGroup)
        }
    }

    @Test
    fun `determineParam will create a default param`() = test {
        given {
            val adapter = TestParamFactoryAdaptor()

            expect {
                TestResponse(
                    "public var test: kotlin.String? = null\n",
                    ""
                )
            }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)

                TestResponse(
                    propSchema.toPropertySpec().toString(),
                    propSchema.accessors().firstOrNull()?.toString() ?: ""
                )
            }
        }
    }

    @Test
    fun `determineParam will create a group param`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(
                LIST.parameterizedBy(Example::class.asTypeName()),
                isGroup = true
            )

            expect {
                TestResponse(
                    "private var test: kotlin.collections.List<test.Example>? = null\n",
                    """
                        |public fun test(block: test.ExampleDslBuilder.Group.() -> kotlin.Unit) {
                        |  this.test = test.ExampleDslBuilder.Group().apply(block).items()
                        |}
                        |
                    """.trimMargin()
                )
            }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)

                TestResponse(
                    propSchema.toPropertySpec().toString(),
                    propSchema.accessors().firstOrNull()?.toString() ?: ""
                )
            }
        }
    }

    class TestParamFactoryAdaptor(
        override val actualPropTypeName: TypeName = STRING,
        val isGroup: Boolean = false
    ) : PropertySchemaFactoryAdapter {
        override val propName: String = "test"
        override val hasSingleEntryTransform: Boolean = false
        override val transformTemplate: String? = null
        override val transformType: TypeName? = null
        override val hasNullableAssignment: Boolean = false
        override val propertyNonNullableClassName: ClassName? = null
        override val hasGeneratedDslAnnotation: Boolean = false
        override val propertyClassDeclarationQualifiedName: String? = null
        override val propertyClassDeclaration: KSClassDeclaration? = null
        override val isGroupElement: Boolean = isGroup
        override val groupElementClassName: ClassName? = ClassName("test", "Example")
        override val groupElementClassDeclaration: KSClassDeclaration? = null
        override var mapDetails: PropertySchemaFactoryAdapter.MapDetails? = null
        override val mapValueClassDeclaration: KSClassDeclaration? = null
    }

    class TestPropDomain(
        override val type: TypeName = STRING,
        val isGroup: Boolean = false
    ) : DomainProperty {
        override fun simpleName(): String = "test"
        override fun continueBranch(): Boolean = false
        override fun singleEntryTransformString(): String? = null
    }

    data class TestResponse(
        val propertyContent: String,
        val functionContent: String
    )

    @GeneratedDsl(
        withListGroup = true
    )
    class Example
}