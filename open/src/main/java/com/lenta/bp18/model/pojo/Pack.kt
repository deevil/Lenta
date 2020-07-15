package com.lenta.bp18.model.pojo

import com.lenta.shared.fmp.resources.dao_ext.DictElement


data class Pack(
        val material: String, // sap-код товара
        val materialOsn: String, // sap-код полуфабриката
        val materialDef: String = "", // sap-код полуфабриката из которого произведен брак
        val code: String,
        val order: String,
        val quantity: Double,
        val isDefOut: Boolean = false,
        val category: DictElement? = null,
        val defect: DictElement? = null
) {

    fun getShortPackNumber(): String {
        var number = code
        while (number.isNotEmpty() && number.startsWith("0")) {
            number = number.substring(1)
        }

        return number
    }

    fun isNotDefect(): Boolean {
        return materialDef.isEmpty()
    }

    fun isDefect(): Boolean {
        return materialDef.isNotEmpty()
    }

}