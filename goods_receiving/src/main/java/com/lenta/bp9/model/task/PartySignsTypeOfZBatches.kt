package com.lenta.bp9.model.task

enum class PartySignsTypeOfZBatches(val partySignsTypeString: String) {
    None(""),
    /** дата производства*/
    ProductionDate("ДП"),
    /** срок годности*/
    ShelfLife("СГ");

    companion object {
        fun from(partySignsTypeString: String): PartySignsTypeOfZBatches {
            return when(partySignsTypeString) {
                "ДП" -> ProductionDate
                "СГ" -> ShelfLife
                else -> None
            }
        }
    }
}