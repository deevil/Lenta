package com.lenta.bp18.model.pojo

import com.lenta.bp18.platform.Constants
import com.lenta.shared.models.core.Uom

data class GoodInfo(
        val id: Int,
        val ean: String?,
        val name: String,
        val material: String = Constants.GOOD_MATERIAL,
        val matcode: String = Constants.GOOD_MATCODE,
        val uom: Uom = Uom.DEFAULT,
        val partNumber: String?,
        val group: String?,
        val condition: String?
)