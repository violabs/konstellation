package io.violabs.konstellation.dsl.props

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import io.violabs.geordi.UnitSim
import io.violabs.konstellation.dsl.schema.GroupPropSchema
import org.junit.jupiter.api.Test

class GroupParamTest : UnitSim() {
    val propTypeName = LIST.parameterizedBy(TestObj::class.asTypeName() as TypeName).copy(nullable = true)
    val groupBuilderName = ClassName("test", "TestObj")

    @Test
    fun `toPropertySpec - happy path`() = test {
        given {
            val param = GroupPropSchema("test", propTypeName, groupBuilderName)

            expect {
                "private var test: kotlin.collections.List<test.TestObj>? = null"
            }

            whenever {
                val propSpec = param.toPropertySpec()

                propSpec.toString().trimIndent()
            }
        }
    }

    @Test
    fun `accessors - happy path`() = test {
        given {
            val param = GroupPropSchema("test", propTypeName, groupBuilderName)

            expect {
                """
                    |public fun test(block: test.TestObjDslBuilder.Group.() -> kotlin.Unit) {
                    |  this.test = test.TestObjDslBuilder.Group().apply(block).items()
                    |}
                """.trimMargin()
            }

            whenever { param.accessors().first().toString().trimIndent() }
        }
    }

    class TestObj
}