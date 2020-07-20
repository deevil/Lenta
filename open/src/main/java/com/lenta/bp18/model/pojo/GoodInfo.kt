package com.lenta.bp18.model.pojo

import com.lenta.bp18.platform.Constants
import com.lenta.shared.models.core.Uom

data class GoodInfo(
        val id: Int,
        val ean: String?,
        val material: String = Constants.GOOD_MATERIAL,
        val matcode: String = Constants.GOOD_MATCODE,
        val enteredCode: EnteredCode,
        val name: String,
        val partNumber: String?,
        val group: String?,
        val condition: String?,
        val uom: Uom = Uom.DEFAULT,
        val status: GoodStatus = GoodStatus.CREATED
)

enum class GoodStatus {
    CREATED,
    PROCESSED,
    MISSING_WRONG,
    MISSING_RIGHT
}