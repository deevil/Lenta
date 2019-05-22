package com.lenta.shared.models.core

import com.google.gson.annotations.SerializedName

class Supplier(@SerializedName("LIFNR") val code: String?, @SerializedName("LIFNR_NAME") val name: String) {

    companion object {
        val DEFAULT = Supplier(null, "Нет поставщика")
        val DEFAULT_SELECT = Supplier(null, "Выберите поставщика (кредитора)")
        val DEFAULT_ALL = Supplier(null, "Все поставщики")
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

        other as Supplier
        return equals(other)
    }

    fun equals(supplier: Supplier?): Boolean {
        return if (supplier == null) false else code == supplier.code
    }
}