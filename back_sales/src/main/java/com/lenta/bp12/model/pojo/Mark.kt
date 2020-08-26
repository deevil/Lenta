package com.lenta.bp12.model.pojo

data class Mark(
        var number: String,
        var boxNumber: String = "",
        var isBadMark: Boolean = false,
        var providerCode: String,
        var maxRetailPrice: String = "",
        var packNumber: String = ""
) {
    var basketNumber = 0

    sealed class Container(number: String, gtin: String) {
        class Mark(number: String, gtin: String) : Container(number, gtin)
        class Pack(number: String, gtin: String) : Container(number, gtin)
        class Box(number: String, gtin: String) : Container(number, gtin)
    }

}
