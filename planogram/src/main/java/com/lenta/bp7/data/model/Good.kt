package com.lenta.bp7.data.model

data class Good(
    val id: Int,
    val sapCode: String = "",
    val barCode: String = "",
    val name: String = "",
    var totalFacings: Int = 0,
    val units: String = "",
    var status: GoodStatus = GoodStatus.CREATED
)

enum class GoodStatus {
    CREATED,
    MISSING,
    PRESENT
}