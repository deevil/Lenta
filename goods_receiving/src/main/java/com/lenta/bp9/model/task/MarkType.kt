package com.lenta.bp9.model.task

enum class MarkType(val markTypeString: String) {
    None(""),
    /** TOBACCO*/
    Tobacco("TOBACCO"),
    /** SHOES*/
    Shoes("SHOES");

    companion object {
        fun from(markTypeString: String): MarkType {
            return when(markTypeString) {
                "TOBACCO" -> Tobacco
                "SHOES" -> Shoes
                else -> None
            }
        }
    }
}

fun getMarkType(code: String): MarkType {
    return when (code) {
        "TOBACCO" -> MarkType.Tobacco
        "SHOES" -> MarkType.Shoes
        else -> MarkType.None
    }
}