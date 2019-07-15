package com.lenta.bp7.data.model

class Good(
    val id: Int,
    val sapCode: String,
    val barCode: String,
    val name: String,
    var totalFacings: Int,
    var status: GoodStatus = GoodStatus.CREATED
)

enum class GoodStatus {
    CREATED,
    MISSING,
    PRESENT
}