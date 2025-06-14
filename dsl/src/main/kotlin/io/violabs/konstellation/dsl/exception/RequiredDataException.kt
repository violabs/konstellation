package io.violabs.konstellation.dsl.exception

class RequiredDataException(identifier: String) : RuntimeException("value is required. value: $identifier")
