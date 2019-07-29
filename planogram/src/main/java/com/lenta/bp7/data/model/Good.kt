package com.lenta.bp7.data.model

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Root

@Root(name = "goods")
data class Good(
        val id: Int,
        @Attribute(name = "SAPCode")
        var sapCodeForSend: String = "",
        val sapCode: String,
        @Attribute(name = "barcode")
        val barCode: String,
        val name: String,
        @Attribute(name = "count", required = false)
        var facings: Int = 0,
        @Attribute(name = "labeled", required = false)
        var labeled: Int = 1,
        val unitsCode: String,
        val units: String,
        private var status: GoodStatus = GoodStatus.CREATED
) {
    // <goods SAPCode="169398_ST" barcode="4820043010028" count="0" labeled="1" />
    init {
        sapCodeForSend = getFormattedSapCode() + "_$unitsCode"
    }

    fun setStatus(status: GoodStatus) {
        this.status = status

        labeled = if (status == GoodStatus.MISSING_WRONG) 0 else 1
    }

    fun getStatus(): GoodStatus {
        return status
    }

    fun getFormattedSapCode(): String {
        return sapCode.takeLast(6)
    }

    fun getNumberOfFacing(): String? {
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