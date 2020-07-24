package com.lenta.shared.models.core

data class Uom(val code: String, val name: String) {

    companion object {
        val DEFAULT = Uom("ST", "шт")
        val ST = Uom("ST", "шт")
        val KAR = Uom("KAR", "кор")
        val G = Uom("G", "г")
        val KG = Uom("KG", "кг")
    }

}

fun Uom.isOnlyInt(): Boolean {
    return this.code != "KG"
}

fun getInnerUnits(units: Uom): Uom {
    return when(units){
        Uom.KG -> Uom.G
        else -> Uom.ST
    }
}