package com.lenta.bp18.model.pojo

import com.lenta.bp18.platform.Constants
import com.lenta.shared.models.core.Uom

data class Good(
        /**EAN*/
        val ean: String,
        /**Код материала*/
        val material: String = Constants.GOOD_MATERIAL,
        /**Sap код товара*/
        val matcode: String = Constants.GOOD_MATCODE,
        /**Название товара*/
        val name: String,
        /**Единицы измерения*/
        val uom: Uom = Uom.DEFAULT
) {
    fun getFormattedMaterial(): String {
        return material.takeLast(6)
    }

    fun getFormattedMatcode(): String {
        return matcode.takeLast(6)
    }
}
