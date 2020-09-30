package com.lenta.shared.models.core

data class Uom(val code: String, val name: String) {

    companion object {

        const val DATA_KG = "KG"
        const val DATA_G = "G"
        const val DATA_KAR = "KAR"
        const val DATA_ST = "ST"
        const val DATA_KG_RUS = "КГ"
        const val DATA_G_RUS = "Г"
        const val DATA_KOR_RUS = "КОР"
        const val DATA_ST_RUS = "ШТ"

        val DEFAULT = Uom(DATA_ST, "шт")
        val ST = Uom(DATA_ST, "шт")
        val KAR = Uom(DATA_KAR, "кор")
        val G = Uom(DATA_G, "г")
        val KG = Uom(DATA_KG, "кг")
    }

}

fun Uom.isOnlyInt(): Boolean {
    return this.code != Uom.DATA_KG && this.code != Uom.DATA_G
}

fun String.toUom(): Uom {
    return when(this) {
        Uom.DATA_KG, Uom.DATA_KG_RUS -> Uom.KG
        Uom.DATA_G, Uom.DATA_G_RUS -> Uom.G
        Uom.DATA_KAR, Uom.DATA_KOR_RUS -> Uom.KAR
        Uom.DATA_ST, Uom.DATA_ST_RUS -> Uom.ST
        else -> Uom.DEFAULT
    }
}

fun getInnerUnits(units: Uom): Uom {
    return when(units){
        Uom.KG -> Uom.G
        else -> Uom.ST
    }
}