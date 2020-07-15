package com.lenta.bp18.data.model

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
) {

    fun setStatus(status: GoodStatus) {
        this.status = status
    }

    fun getStatus(): GoodStatus {
        return status
    }

    fun getFormattedMaterial(): String? {
        return material?.takeLast(6)
    }

    fun getFacingOrPlus(): String? {
        return if ((status == GoodStatus.CREATED || status == GoodStatus.PROCESSED) && facings == 0) "+" else facings.toString()
    }

    fun getEanOrEmpty(): String {
        return if (enteredCode == EnteredCode.EAN) ean.orEmpty() else ""
    }

}

data class GoodInfo(
        val ean: String,
        val material: String = "000000000000000000",
        val matcode: String = "000000000000",
        val enteredCode: EnteredCode,
        val name: String = "<НЕ ОПРЕДЕЛЕН>",
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