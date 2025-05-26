package io.violabs.konstellation.generateTest

import io.violabs.konstellation.metaDsl.annotation.SingleEntryTransformDsl

@SingleEntryTransformDsl<String>(String::class)
data class Stardate(val content: String)