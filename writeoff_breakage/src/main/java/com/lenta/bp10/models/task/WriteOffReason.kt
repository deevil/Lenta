package com.lenta.bp10.models.task

data class WriteOffReason(val code: String, val name: String) {
    companion object {
        val empty by lazy {
            WriteOffReason(code = "", name = "")
        }
    }
}