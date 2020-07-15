package com.lenta.bp18.data.model

import com.lenta.bp18.platform.Constants
import com.lenta.shared.models.core.Uom

data class Good(
        val id: Int,
        val ean: String?,
        val material: String?,
        val matcode: String?,
        val enteredCode: EnteredCode,
        val name: String,
        var facings: Int = 0,
        val uom: Uom = Uom.DEFAULT,
        private var status: GoodStatus = GoodStatus.CREATED
)

data class GoodInfo(
        val ean: String,
        val material: String = Constants.GOOD_INFO_MATERIAL,
        val matcode: String = Constants.GOOD_INFO_MATCODE,
        val enteredCode: EnteredCode,
        val name: String = Constants.GOOD_INFO_NAME,
        val uom: Uom = Uom.DEFAULT
)

enum class GoodStatus {
    CREATED,
    PROCESSED,
    MISSING_WRONG,
    MISSING_RIGHT
}

enum class EnteredCode {
    EAN,
    MATERIAL,
    MATCODE
}