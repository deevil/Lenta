package com.lenta.bp15.model.enum

enum class ShoesMarkType {

    UNKNOWN,
    MAN,
    WOMAN,
    CHILDREN,
    UNISEX;

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