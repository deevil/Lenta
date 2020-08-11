package com.lenta.shared.models.core

data class Uom(val code: String, val name: String) {

    companion object {

        const val DATA_KG = "KG"
        const val DATA_G = "G"
        const val DATA_KAR = "KAR"
        const val DATA_ST = "ST"

        val DEFAULT = Uom(DATA_ST, "шт")
        val ST = Uom(DATA_ST, "шт")
        val KAR = Uom(DATA_KAR, "кор")
        val G = Uom(DATA_G, "г")
        val KG = Uom(DATA_KG, "кг")
    }

}

fun Uom.isOnlyInt(): Boolean {
    return this.code != Uom.DATA_KG
}

fun String.toUom(): Uom {
    return when(this) {
        Uom.DATA_KG -> Uom.KG
        Uom.DATA_G -> Uom.G
        Uom.DATA_KAR -> Uom.KAR
        Uom.DATA_ST -> Uom.ST
        else -> Uom.DEFAULT
    }
}

fun getInnerUnits(units: Uom): Uom {
    return when(units){
        Uom.KG -> Uom.G
        else -> Uom.ST
    }
}