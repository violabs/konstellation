package io.violabs.konstellation.dsl.props

import com.squareup.kotlinpoet.STRING
import io.violabs.geordi.UnitSim
import io.violabs.konstellation.dsl.schema.ListPropSchema
import org.junit.jupiter.api.Test

class ListParamTest : UnitSim() {

    @Test
    fun `toPropertySpec - happy path`() = test {
        given {
            val param = ListPropSchema("test", STRING)

            expect { "private var test: kotlin.collections.List<kotlin.String>? = null" }

            whenever {
                val propSpec = param.toPropertySpec()

                propSpec.toString().trimIndent()
            }
        }
    }

    @Test
    fun `accessors - happy path`() = test {
        given {
            val param = ListPropSchema("test", nullableAssignment = true)

            expect {
                """
                    |public fun test(vararg items: kotlin.String) {
                    |  this.test = items.toList()
                    |}
                """.trimMargin()
            }

            whenever { param.accessors().first().toString().trimIndent() }
        }
    }
}