package com.lenta.bp15.model.enum

enum class ShoesMarkType(val code: String, val description: String) {

    UNKNOWN("","Unknown type"),
    MAN("M","Обувь мужская"),
    WOMAN("F","Обувь женская"),
    CHILDREN("C","Обувь детская"),
    UNISEX("O","Обувь унисекс");

    companion object {
        fun from(code: String): ShoesMarkType {
            return when (code) {
                "M" -> MAN
                "F" -> WOMAN
                "C" -> CHILDREN
                "O" -> UNISEX
                else -> UNKNOWN
            }
        }
    }

}