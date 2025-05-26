package io.violabs.konstellation.generateTest

import io.violabs.geordi.UnitSim
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class GenerateTest : UnitSim() {
    private val starShipName = "USS Enterprise NCC-1701-D"
    private val crusherName = "Beverly Crusher"
    private val picardName = "Jean-Luc Picard"
    private val rikerName = "William T. Riker"
    private val worfName = "Worf"
    private val crusher = Passenger(name = crusherName, rank = Passenger.Rank.COMMANDER)
    private val picard = Passenger(name = picardName, rank = Passenger.Rank.CAPTAIN)
    private val riker = Passenger(name = rikerName, rank = Passenger.Rank.COMMANDER)
    private val worf = Passenger(name = "Worf", rank = Passenger.Rank.LIEUTENANT_COMMANDER)

    @Test
    fun `generates a full class - happy path`() = test {
        given {
            val now = LocalDateTime.now()
            expect {
                StarShip(
                    name = starShipName,
                    commanderNames = listOf(rikerName, crusherName),
                    crewMap = mapOf(
                        Passenger.Rank.COMMANDER.name to crusher,
                        Passenger.Rank.CAPTAIN.name to picard
                    ),
                    description = "Target ship",
                    activated = true,
                    docked = false,
                    capacity = 200,
                    coordinates = SpaceTime(
                        space = Space(x = 100f, y = 100f, z = 100f),
                        time = now
                    ),
                    stardate = Stardate("10000.1"),
                    // map based primitives (vararg)
                    // map based primitives (add)
                    // map based complex (add w/ builder)
                    passengers = listOf(crusher, worf),
                    areaCodes = mapOf(
                        "A1" to "Artax 1",
                        "VLS" to "Violet Lunar Station"
                    ),
                    roomMap = mapOf(
                        "1" to picard,
                        "2" to riker
                    ),
                    notes = listOf("Needs some work")
                )
            }

            whenever {
                starShip {
                    val crusher: PassengerDslBuilderScope = {
                        name = crusherName
                        rank = Passenger.Rank.COMMANDER
                    }

                    val picard: PassengerDslBuilderScope = {
                        name = picardName
                        rank = Passenger.Rank.CAPTAIN
                    }

                    val riker: PassengerDslBuilderScope = {
                        name = rikerName
                        rank = Passenger.Rank.COMMANDER
                    }

                    val worf: PassengerDslBuilderScope = {
                        name = worfName
                        rank = Passenger.Rank.LIEUTENANT_COMMANDER
                    }

                    name = starShipName
                    commanderNames(rikerName, crusherName)
                    crewMap {
                        passenger(Passenger.Rank.COMMANDER.name, crusher)
                        passenger(Passenger.Rank.CAPTAIN.name, picard)
                    }
                    description = "Target ship"
                    activated()
                    docked(false)
                    capacity = 200
                    coordinates {
                        space { x = 100f; y = 100f; z = 100f }
                        time = now
                    }
                    stardate("10000.1")
                    passengers {
                        passenger(crusher)
                        passenger(worf)
                    }
                    areaCodes(
                        "A1" to "Artax 1",
                        "VLS" to "Violet Lunar Station"
                    )
                    roomMap {
                        passenger("1", picard)
                        passenger("2", riker)
                    }
                    notes("Needs some work")
                }
            }
        }
    }

    @Test
    fun `requireNotNull will throw an exception`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>(
                "name is required"
            ) {
                starShip {}
            }
        }
    }

    @Test
    fun `requireCollectionNotEmpty will throw an exception if null`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>(
                "commanderNames is required and cannot be empty"
            ) {
                starShip {
                    name = starShipName
                }
            }
        }
    }

    @Test
    fun `requireCollectionNotEmpty will throw an exception if empty`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>(
                "commanderNames is required and cannot be empty"
            ) {
                starShip {
                    name = starShipName
                    commanderNames()
                }
            }
        }
    }

    @Test
    fun `requireMapNotEmpty will throw an exception if null`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>(
                "crewMap is required and cannot be empty"
            ) {
                starShip {
                    name = starShipName
                    commanderNames("test")
                }
            }
        }
    }

    @Test
    fun `requireMapNotEmpty will throw an exception if empty`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>(
                "crewMap is required and cannot be empty"
            ) {
                starShip {
                    name = starShipName
                    commanderNames("test")
                    crewMap {  }
                }
            }
        }
    }
}