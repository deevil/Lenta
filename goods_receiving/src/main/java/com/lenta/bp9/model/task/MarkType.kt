package com.lenta.bp9.model.task

private const val MARK_TYPE_TOBACCO = "TOBACCO"
private const val MARK_TYPE_SHOES = "SHOES"
enum class MarkType(val markTypeString: String) {
    None(""),
    /** TOBACCO*/
    Tobacco(MARK_TYPE_TOBACCO),
    /** SHOES*/
    Shoes(MARK_TYPE_SHOES);

    companion object {

        fun from(markTypeString: String): MarkType {
            return when(markTypeString) {
                MARK_TYPE_TOBACCO -> Tobacco
                MARK_TYPE_SHOES -> Shoes
                else -> None
            }
        }
    }
}

fun getMarkType(code: String): MarkType {
    return when (code) {
        MARK_TYPE_TOBACCO -> MarkType.Tobacco
        MARK_TYPE_SHOES -> MarkType.Shoes
        else -> MarkType.None
    }
}