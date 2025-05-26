package io.violabs.konstellation.dsl.props

import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import io.violabs.geordi.UnitSim
import io.violabs.konstellation.dsl.schema.SingleTransformPropSchema
import org.junit.jupiter.api.Test

private class TestCase

class SingleTransformParamTest : UnitSim() {
    val propTypeName = TestCase::class.asTypeName()
    val inputTypeName = String::class.asTypeName()
    private val testResponseClassName = TestCase::class.asClassName()

    @Test
    fun `toPropertySpec - happy path`() = test {
        given {
            val param = SingleTransformPropSchema(
                "test",
                inputTypeName,
                propTypeName,
            )

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
            val param = SingleTransformPropSchema(
                "test",
                inputTypeName,
                propTypeName,
                nullableAssignment = true
            )

            expect {
                """
                    |public fun test(test: kotlin.String) {
                    |  this.test = $testResponseClassName(test)
                    |}
                """.trimMargin()
            }

            whenever { param.accessors().first().toString().trimIndent() }
        }
    }
}