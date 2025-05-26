package io.violabs.konstellation.generateTest

import io.violabs.konstellation.metaDsl.annotation.GeneratedDsl
import java.time.LocalDateTime

@GeneratedDsl
data class SpaceTime(
    val space: Space? = null,
    val time: LocalDateTime? = null
)