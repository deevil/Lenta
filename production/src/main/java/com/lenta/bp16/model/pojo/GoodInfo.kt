package com.lenta.bp16.model.pojo

import com.lenta.bp16.platform.Constants
import com.lenta.shared.models.core.Uom

data class GoodInfo(
        val ean: String,
        val material: String = Constants.GOOD_MATERIAL,
        val matcode: String = Constants.GOOD_MATCODE,
        val name: String,
        val uom: Uom = Uom.DEFAULT
) {
    fun getFormattedMaterial(): String {
        return material.takeLast(6)
    }
}