package com.lenta.bp16.model.ingredients.params

import com.google.gson.annotations.SerializedName

/** Разблокировка объекта */
data class UnblockIngredientsParams(
        /** Номер объекта */
        @SerializedName("IV_OBJ_CODE")
        val code: String,

        /**
        Режим обработки:
        1 – Разблокировка ЕО
        2 – Разблокировка ВП
         */
        @SerializedName("IV_MODE ")
        val mode: String
) {
    companion object {
        const val MODE_UNBLOCK_EO = "1"
        const val MODE_UNBLOCK_VP = "2"
        const val MODE_UNBLOCK_ORDER = "4"
        const val MODE_UNBLOCK_MATERIAL = "5"
    }
}