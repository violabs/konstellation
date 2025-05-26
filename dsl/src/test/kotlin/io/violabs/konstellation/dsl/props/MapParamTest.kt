package io.violabs.konstellation.dsl.props

import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.STRING
import io.violabs.geordi.UnitSim
import io.violabs.konstellation.dsl.schema.MapPropSchema
import org.junit.jupiter.api.Test

class MapParamTest : UnitSim() {

    @Test
    fun `toPropertySpec - happy path`() = test {
        given {
            val param = MapPropSchema("test", STRING, INT)

            expect { "private var test: kotlin.collections.Map<kotlin.String, kotlin.Int>? = null" }

            whenever {
                val propSpec = param.toPropertySpec()

                propSpec.toString().trimIndent()
            }
        }
    }

    @Test
    fun `accessors - happy path`() = test {
        given {
            val param = MapPropSchema("test", STRING, INT, nullableAssignment = true)

            expect {
                """
                    |public fun test(vararg items: kotlin.Pair<kotlin.String, kotlin.Int>) {
                    |  this.test = items.toMap()
                    |}
                """.trimMargin()
            }

            whenever { param.accessors().first().toString().trimIndent() }
        }
    }
}