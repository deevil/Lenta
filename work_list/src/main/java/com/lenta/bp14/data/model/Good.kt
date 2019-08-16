package com.lenta.bp14.data.model


data class Good(
        val material: String?,
        val name: String,
        val total: Int = 0
) {

    fun getFormattedMaterial(): String? {
        return material?.takeLast(6)
    }

}