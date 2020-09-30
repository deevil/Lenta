package com.lenta.bp10.models.task


data class WriteOffReason(
        val code: String,
        val name: String,
        val gisControl: String
) {
    companion object {
        fun emptyWithTitle(emptyCategory: String): WriteOffReason {
            return WriteOffReason(code = "", name = emptyCategory, gisControl = "")
        }

        val empty by lazy {
            WriteOffReason(code = "", name = "", gisControl = "")
        }
    }
}