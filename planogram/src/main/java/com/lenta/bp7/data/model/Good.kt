package com.lenta.bp7.data.model

data class Good(
        val id: Int,
        val ean: String?,
        val material: String?,
        val matcode: String?,
        val enteredCode: EnteredCode,
        val name: String,
        var facings: Int = 0,
        val unitsCode: String,
        val units: String,
        private var status: GoodStatus = GoodStatus.CREATED
) {

    fun setStatus(status: GoodStatus) {
        this.status = status
    }

    fun getStatus(): GoodStatus {
        return status
    }

    fun getFormattedSapCode(): String? {
        return material?.takeLast(6)
    }

    fun getFacingOrPlus(): String? {
        return if ((status == GoodStatus.CREATED || status == GoodStatus.PROCESSED) && facings == 0) "+" else facings.toString()
    }

}

data class GoodInfo(
        val ean: String,
        val material: String = "000000000000000000",
        val matcode: String = "000000000000",
        val enteredCode: EnteredCode,
        val name: String = "<НЕ ОПРЕДЕЛЕН>",
        val unitsCode: String = "ST",
        val units: String = "шт"
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