package io.violabs.geordi

import io.violabs.geordi.exceptions.NotFoundException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

private typealias StringProperties = UnitSim.TestSlice<String>.DynamicProperties<String>
private typealias StringProvider = (props: StringProperties) -> String

/**
 * Takes in a json string and formats it to match the expected json format
 * @param givenFn a function that takes in a StringProperties object and returns a json string
 */
fun UnitSim.TestSlice<String>.GivenBuilder.expectJson(
    slice: UnitSim.TestSlice<String>,
    givenFn: StringProvider
): Unit = expect {
    slice.compressJson(givenFn)
}

/**
 * Takes in a json string and formats it to match the expected json format
 * @param whenFn a function that takes in a StringProperties object and returns a json string
 */
fun UnitSim.TestSlice<String>.GivenBuilder.wheneverJson(
    slice: UnitSim.TestSlice<String>,
    whenFn: StringProvider
): Unit = whenever {
    slice.compressJson(whenFn)
}

/**
 * Compresses a json string to match the expected json format.
 * @param stringProvider a function that takes in a StringProperties object and returns a json string
 * @return a compressed json string
 * @throws NotFoundException.JsonMapper if the json mapper is not found
 */
private fun UnitSim.TestSlice<String>.compressJson(stringProvider: StringProvider): String {
    val mapper: Json = json ?: throw NotFoundException.JsonMapper

    val mapObject = mapper.parseToJsonElement(stringProvider(objectProvider))
    return mapper.encodeToString(JsonObject.serializer(), mapObject.jsonObject)
}
