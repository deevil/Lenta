package com.lenta.bp15.model.enum

enum class ShoesMarkType(val description: String) {

    UNKNOWN("Unknown type"),
    MAN("Обувь мужская"),
    WOMAN("Обувь женская"),
    CHILDREN("Обувь детская"),
    UNISEX("Обувь унисекс");

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