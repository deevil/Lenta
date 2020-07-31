package com.lenta.bp18.model.pojo

import com.lenta.bp18.platform.Constants
import com.lenta.shared.models.core.Uom

data class Good(
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
