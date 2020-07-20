package com.lenta.bp18.model.pojo

import com.lenta.bp18.platform.Constants
import com.lenta.shared.models.core.BarcodeInfo
import com.lenta.shared.models.core.Uom

data class Good(
        val ean: String?,
        val name: String,
        val material: String = Constants.GOOD_MATERIAL,
        val matcode: String = Constants.GOOD_MATCODE,
        val uom: Uom = Uom.DEFAULT,
        val enteredCode: EnteredCode?
)

enum class EnteredCode {
    EAN,
    MATERIAL,
    MATCODE
}