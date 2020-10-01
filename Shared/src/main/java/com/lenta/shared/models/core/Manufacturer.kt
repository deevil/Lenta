package com.lenta.shared.models.core

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Manufacturer(
        @SerializedName("ZPROD")
        val code: String,
        @SerializedName("PROD_NAME")
        val name: String
) : Parcelable {

    companion object {
        val DEFAULT = Manufacturer("", "Нет производителя")
    }

    override fun toString(): String {
        return name
    }

    override fun hashCode(): Int {
        return code.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Manufacturer

        return equals(other)
    }

    fun equals(manufacturer: Manufacturer?): Boolean {
        return if (manufacturer == null) false else code == manufacturer.code
    }
}