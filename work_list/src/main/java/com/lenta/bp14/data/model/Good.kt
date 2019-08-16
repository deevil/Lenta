package com.lenta.bp14.data.model


data class Good(
        val material: String?,
        val name: String
) {

    fun getFormattedMaterial(): String? {
        return material?.takeLast(6)
    }

}