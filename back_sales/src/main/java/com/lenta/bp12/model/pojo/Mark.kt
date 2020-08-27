package com.lenta.bp12.model.pojo

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Mark(
        var number: String,
        var boxNumber: String = "",
        var isBadMark: Boolean = false,
        var providerCode: String = "",
        var maxRetailPrice: String = "",
        var packNumber: String = ""
) : Parcelable {
    @IgnoredOnParcel
    var basketNumber = 0

    enum class Container {
        SHOE,
        CARTON,
        BOX
    }

}
