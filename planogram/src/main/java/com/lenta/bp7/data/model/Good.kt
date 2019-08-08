package com.lenta.bp7.data.model

data class Good(
        val id: Int,
        val sapCode: String,
        val barCode: String,
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

    fun getFormattedSapCode(): String {
        return sapCode.takeLast(6)
    }

    fun getFacingOrPlus(): String? {
        return if ((status == GoodStatus.CREATED || status == GoodStatus.PROCESSED) && facings == 0) "+" else facings.toString()
    }

}

data class GoodInfo(
        val sapCode: String = "000000",
        val barCode: String,
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