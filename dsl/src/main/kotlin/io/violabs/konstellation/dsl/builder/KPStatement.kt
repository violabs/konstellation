package io.violabs.konstellation.dsl.builder

@PicardDSLMarker
class KPStatement(
    var statement: String,
    var args: MutableList<Any> = mutableListOf()
) {

    @PicardDSLMarker
    class Group {
        val items: MutableList<KPStatement> = mutableListOf()

        fun addLine(statement: String, vararg args: Any) {
            items.add(KPStatement(statement, args.toMutableList()))
        }
    }
}