package io.violabs.konstellation.dsl.props

import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import io.violabs.geordi.UnitSim
import io.violabs.konstellation.dsl.schema.MapGroupPropSchema
import org.junit.jupiter.api.Test

class MapBuilderParamTest : UnitSim() {

    @Test
    fun `toPropertySpec - happy path`() = test {
        given {
            val param = MapGroupPropSchema("test", STRING, TestObj::class.asTypeName() as TypeName)

            expect { "private var test: kotlin.collections.Map<kotlin.String, io.violabs.konstellation.dsl.props.MapBuilderParamTest.TestObj>? = null" }

            whenever {
                val propSpec = param.toPropertySpec()

                propSpec.toString().trimIndent()
            }
        }
    }

    @Test
    fun `accessors - happy path`() = test {
        given {
            val param = MapGroupPropSchema(
                "test",
                STRING, TestObj::class.asTypeName() as TypeName,
                false
            )

            expect {
                """
                    |public fun test(block: io.violabs.konstellation.dsl.props.TestObjDslBuilder.MapGroup<kotlin.String>.() -> kotlin.Unit) {
                    |  this.test = io.violabs.konstellation.dsl.props.TestObjDslBuilder.MapGroup<kotlin.String>().apply(block).items().toMap()
                    |}
                """.trimMargin()
            }

            whenever { param.accessors().first().toString().trimIndent() }
        }
    }

    class TestObj
}