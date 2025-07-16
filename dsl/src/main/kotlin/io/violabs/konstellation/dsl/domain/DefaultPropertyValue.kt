package io.violabs.konstellation.dsl.domain

import com.squareup.kotlinpoet.CodeBlock
import kotlin.reflect.KClass

data class DefaultPropertyValue(
    val rawValue: String,
    val codeBlock: CodeBlock,
    val classRef: KClass<*> = String::class
)
