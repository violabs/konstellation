package io.violabs.konstellation.generateTest

import io.violabs.geordi.UnitSim
import io.violabs.konstellation.generateTest.nested.Version
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
                    notes = listOf("Needs some work"),
                    defaultString = "DEFAULT",
                    version = Version.V1
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

    // ========== @DslProperty Tests ==========

    @Test
    fun `DslProperty - default generates both vararg and provider for lists`() = test {
        given {
            val expectedAliases = listOf("NCC-1701-D", "Enterprise-D", "Flagship")

            expect {
                StarShip(
                    name = starShipName,
                    commanderNames = listOf(rikerName),
                    crewMap = mapOf(Passenger.Rank.CAPTAIN.name to picard),
                    aliases = expectedAliases
                )
            }

            // Test vararg function
            whenever {
                starShip {
                    name = starShipName
                    commanderNames(rikerName)
                    crewMap { passenger(Passenger.Rank.CAPTAIN.name) { name = picardName; rank = Passenger.Rank.CAPTAIN } }
                    aliases("NCC-1701-D", "Enterprise-D", "Flagship")
                }
            }
        }
    }

    @Test
    fun `DslProperty - default generates provider function for lists`() = test {
        given {
            val expectedAliases = listOf("NCC-1701-D", "Enterprise-D", "Flagship")

            expect {
                StarShip(
                    name = starShipName,
                    commanderNames = listOf(rikerName),
                    crewMap = mapOf(Passenger.Rank.CAPTAIN.name to picard),
                    aliases = expectedAliases
                )
            }

            // Test provider function - uses receiver syntax (MutableList.() -> Unit)
            whenever {
                starShip {
                    name = starShipName
                    commanderNames(rikerName)
                    crewMap { passenger(Passenger.Rank.CAPTAIN.name) { name = picardName; rank = Passenger.Rank.CAPTAIN } }
                    aliases { addAll(listOf("NCC-1701-D", "Enterprise-D", "Flagship")) }
                }
            }
        }
    }

    @Test
    fun `DslProperty - withProvider false generates only vararg function`() = test {
        given {
            val expectedTags = listOf("federation", "flagship", "galaxy-class")

            expect {
                StarShip(
                    name = starShipName,
                    commanderNames = listOf(rikerName),
                    crewMap = mapOf(Passenger.Rank.CAPTAIN.name to picard),
                    tags = expectedTags
                )
            }

            // Test vararg function (provider should not exist)
            whenever {
                starShip {
                    name = starShipName
                    commanderNames(rikerName)
                    crewMap { passenger(Passenger.Rank.CAPTAIN.name) { name = picardName; rank = Passenger.Rank.CAPTAIN } }
                    tags("federation", "flagship", "galaxy-class")
                }
            }
        }
    }

    @Test
    fun `DslProperty - withVararg false generates only provider function for maps`() = test {
        given {
            val expectedMetadata = mapOf("class" to "Galaxy", "registry" to "NCC-1701-D")

            expect {
                StarShip(
                    name = starShipName,
                    commanderNames = listOf(rikerName),
                    crewMap = mapOf(Passenger.Rank.CAPTAIN.name to picard),
                    metadata = expectedMetadata
                )
            }

            // Test provider function (vararg should not exist) - uses receiver syntax (MutableMap.() -> Unit)
            whenever {
                starShip {
                    name = starShipName
                    commanderNames(rikerName)
                    crewMap { passenger(Passenger.Rank.CAPTAIN.name) { name = picardName; rank = Passenger.Rank.CAPTAIN } }
                    metadata { 
                        this["class"] = "Galaxy"
                        this["registry"] = "NCC-1701-D"
                    }
                }
            }
        }
    }

    @Test
    fun `DslProperty - both false requires direct property assignment`() = test {
        given {
            // When both withVararg and withProvider are false, no accessor functions are generated
            // The property can only be set via direct assignment (which requires access to protected member)
            // or the property remains null
            expect {
                StarShip(
                    name = starShipName,
                    commanderNames = listOf(rikerName),
                    crewMap = mapOf(Passenger.Rank.CAPTAIN.name to picard),
                    systemCodes = null // No accessor functions means it stays null via DSL
                )
            }

            whenever {
                starShip {
                    name = starShipName
                    commanderNames(rikerName)
                    crewMap { passenger(Passenger.Rank.CAPTAIN.name) { name = picardName; rank = Passenger.Rank.CAPTAIN } }
                    // Note: systemCodes has no accessor functions (withVararg=false, withProvider=false)
                    // so there's no way to set it via the DSL builder methods
                }
            }
        }
    }

    @Test
    fun `DslProperty - combined usage with all configurations`() = test {
        given {
            expect {
                StarShip(
                    name = starShipName,
                    commanderNames = listOf(rikerName, crusherName),
                    crewMap = mapOf(Passenger.Rank.CAPTAIN.name to picard),
                    // Default: using provider
                    aliases = listOf("Enterprise", "Flagship"),
                    // withProvider=false: using vararg
                    tags = listOf("starfleet", "exploration"),
                    // withVararg=false: using provider
                    metadata = mapOf("sector" to "001", "quadrant" to "Alpha")
                )
            }

            whenever {
                starShip {
                    name = starShipName
                    commanderNames(rikerName, crusherName)
                    crewMap { passenger(Passenger.Rank.CAPTAIN.name) { name = picardName; rank = Passenger.Rank.CAPTAIN } }
                    // Using provider function (default config) - receiver syntax MutableList.() -> Unit
                    aliases { addAll(listOf("Enterprise", "Flagship")) }
                    // Using vararg function (withProvider=false)
                    tags("starfleet", "exploration")
                    // Using provider function (withVararg=false) - receiver syntax MutableMap.() -> Unit
                    metadata { 
                        this["sector"] = "001"
                        this["quadrant"] = "Alpha"
                    }
                }
            }
        }
    }
}
