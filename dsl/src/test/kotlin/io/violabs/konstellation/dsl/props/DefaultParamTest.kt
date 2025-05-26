package io.violabs.konstellation.dsl.props

import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import io.violabs.geordi.UnitSim
import io.violabs.konstellation.dsl.schema.DefaultPropSchema
import org.junit.jupiter.api.Test

class DefaultParamTest : UnitSim() {
    val propTypeName = String::class.asTypeName()
    private val testResponseClassName = String::class.asClassName()

    @Test
    fun `toPropertySpec - happy path - #scenario`() = test {
        given {
            val param = DefaultPropSchema("test", propTypeName)

            expect { "public var test: $testResponseClassName? = null" }

            whenever {
                val propSpec = param.toPropertySpec()

                propSpec.toString().trimIndent()
            }
        }
    }

    @Test
    fun `accessors - happy path`() = test {
        given {
            val param = DefaultPropSchema("test", propTypeName, true)

            expect { true }

            whenever { param.accessors().isEmpty() }
        }
    }
}