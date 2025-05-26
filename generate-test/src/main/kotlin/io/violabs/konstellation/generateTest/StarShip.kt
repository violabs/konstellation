package io.violabs.konstellation.generateTest

import io.violabs.konstellation.metaDsl.annotation.GeneratedDsl

@GeneratedDsl(
    isRoot = true
)
data class StarShip(
    val name: String,
    val commanderNames: List<String>,
    val crewMap: Map<String, Passenger>,
    val description: String? = null,
    val activated: Boolean? = null,
    val docked: Boolean? = null,
    val capacity: Int? = null,
    val coordinates: SpaceTime? = null,
    val stardate: Stardate? = null,
    val notes: List<String>? = null,
    val passengers: List<Passenger>? = null,
    val areaCodes: Map<String, String>? = null,
    val roomMap: Map<String, Passenger>? = null
)