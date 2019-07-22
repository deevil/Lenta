package com.lenta.bp7.data.model

data class Good(
    val id: Int,
    val sapCode: String? = "",
    val barCode: String? = "",
    val name: String? = "",
    var totalFacings: Int = 0,
    val units: String? = "",
    var status: GoodStatus = GoodStatus.CREATED
) {

    fun getFormattedSapCode(): String? {
        return if (sapCode?.isNotEmpty() == true) sapCode.substring(sapCode.length - 6) else ""
    }

}

data class GoodInfo(
        val sapCode: String? = "",
        val barCode: String? = "",
        val name: String? = "",
        val units: String? = ""
)

enum class GoodStatus {
    CREATED,
    MISSING,
    PRESENT
}