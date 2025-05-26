package io.violabs.konstellation.dsl.props

import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import io.violabs.geordi.UnitSim
import io.violabs.konstellation.dsl.schema.BuilderPropSchema
import org.junit.jupiter.api.Test

class BuilderParamTest : UnitSim() {
    val typeName = TestResponse::class.asTypeName()
    val buildClassName = TestBuilder::class.asClassName()
    private val testResponseClassName = TestResponse::class.asClassName()

    @Test
    fun `toPropertySpec - happy path`() = test {
        given {
            val param = BuilderPropSchema("test", typeName, buildClassName)

            expect { "private var test: $testResponseClassName? = null" }

            whenever {
                val propSpec = param.toPropertySpec()

                propSpec.toString().trimIndent()
            }
        }
    }

    @Test
    fun `accessors - happy path`() = test {
        given {
            val param = BuilderPropSchema("test", typeName, buildClassName, true)

            expect {
                """
                    |public fun test(block: io.violabs.konstellation.dsl.props.BuilderParamTest.TestBuilder.() -> kotlin.Unit) {
                    |  val builder = io.violabs.konstellation.dsl.props.BuilderParamTest.TestBuilder()
                    |  builder.block()
                    |  this.test = builder.build()
                    |}
                """.trimMargin()
            }

            whenever { param.accessors().first().toString().trimIndent() }
        }
    }

    class TestResponse
    class TestBuilder
}