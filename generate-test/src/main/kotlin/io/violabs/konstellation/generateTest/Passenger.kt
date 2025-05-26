package io.violabs.konstellation.generateTest

import io.violabs.konstellation.metaDsl.annotation.GeneratedDsl

@GeneratedDsl(
    withListGroup = true,
    withMapGroup = "SINGLE"
)
data class Passenger(val name: String, val rank: Rank) {

    enum class Rank {
        CAPTAIN,
        CREWMEMBER,
        COMMANDER,
        LIEUTENANT_COMMANDER,
        FIRST_OFFICER,
        SECOND_OFFICER,
        CIVILIAN
    }
}