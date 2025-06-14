package io.violabs.konstellation.dsl.builder

/**
 * A class representing a statement in the KotlinPoet.
 */
@PicardDSLMarker
class KPStatement(
    // The statement to be executed, typically a Kotlin code line.
    var statement: String,
    // A list of arguments to be used in the statement.
    var args: MutableList<Any> = mutableListOf()
) {

    /**
     * A group of statements that can be added to a DSL element.
     */
    @PicardDSLMarker
    class Group {
        val items: MutableList<KPStatement> = mutableListOf()

        /**
         * Adds a statement to the list
         *
         * @param statement The Kotlin code line to be added.
         * @param args A variable number of arguments to be used in the statement.
         */
        fun addLine(statement: String, vararg args: Any) {
            items.add(KPStatement(statement, args.toMutableList()))
        }
    }
}
