package io.violabs.konstellation.metaDsl.annotation

/**
 * Enum representing the types of map groups.
 */
enum class MapGroupType {
    NONE, SINGLE, LIST, ALL;

    companion object {
        val ACTIVE_TYPES: List<MapGroupType?> = listOf(SINGLE, LIST, ALL)
    }
}
