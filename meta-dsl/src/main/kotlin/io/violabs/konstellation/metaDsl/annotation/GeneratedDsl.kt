package io.violabs.konstellation.metaDsl.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class GeneratedDsl(
    val withListGroup: Boolean = false,
    val withMapGroup: String = MapGroupTypes.NONE,
    val isRoot: Boolean = false
) {
    object MapGroupTypes {
        const val NONE = "NONE"
        const val SINGLE = "SINGLE"
        const val LIST = "LIST"
        const val ALL = "ALL"
    }

    enum class MapGroupType {
        NONE, SINGLE, LIST, ALL;

        companion object {
            val ACTIVE_TYPES: List<MapGroupType?> = listOf(SINGLE, LIST, ALL)
        }
    }
}