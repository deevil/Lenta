package com.lenta.bp7.data.model

class Good(
    val id: Int,
    val sapCode: String,
    val barCode: String,
    val name: String,
    var totalFacings: Int,
    var status: GoodStatus = GoodStatus.STARTED
)

enum class GoodStatus {
    STARTED,
    MISSING,
    PRESENT
}