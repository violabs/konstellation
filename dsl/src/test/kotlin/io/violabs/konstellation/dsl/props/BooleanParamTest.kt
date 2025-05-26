package io.violabs.konstellation.dsl.props

import io.violabs.geordi.UnitSim
import io.violabs.konstellation.dsl.schema.BooleanPropSchema
import org.junit.jupiter.api.Test

class BooleanParamTest : UnitSim() {

    @Test
    fun `toPropertySpec - happy path - #scenario`() = test {
        given {
            val param = BooleanPropSchema("test")

            expect { "private var test: kotlin.Boolean? = null" }

            whenever {
                val propSpec = param.toPropertySpec()

                propSpec.toString().trimIndent()
            }
        }
    }

    @Test
    fun `accessors - happy path`() = test {
        given {
            val param = BooleanPropSchema("test", true)

            expect {
                """
                    |public fun test(on: kotlin.Boolean = true) {
                    |  this.test = on
                    |}
                """.trimMargin()
            }

            whenever { param.accessors().first().toString().trimIndent() }
        }
    }
}