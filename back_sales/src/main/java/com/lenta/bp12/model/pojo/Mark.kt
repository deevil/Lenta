package com.lenta.bp12.model.pojo

data class Mark(
        var number: String,
        var boxNumber: String = "",
        var isBadMark: Boolean = false,
        var providerCode: String = "",
        var maxRetailPrice: String = "",
        var packNumber: String = ""
) {
    var basketNumber = 0

    enum class Container {
        SHOE,
        CARTON,
        BOX
    }

}
