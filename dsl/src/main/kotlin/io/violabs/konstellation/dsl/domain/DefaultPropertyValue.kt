package io.violabs.konstellation.dsl.domain

import com.squareup.kotlinpoet.CodeBlock

data class DefaultPropertyValue(
    val rawValue: String,
    val codeBlock: CodeBlock,
    val classRef: String = "java.lang.String"
)
