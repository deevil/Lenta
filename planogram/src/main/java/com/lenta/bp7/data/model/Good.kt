package com.lenta.bp7.data.model

class Good(
    val id: Int,
    val sapCode: Int,
    val barCode: Long,
    val name: String,
    var totalFacings: Int,
    var sign: GoodSign
)

enum class GoodSign {
    MISSING,
    PRESENT
}