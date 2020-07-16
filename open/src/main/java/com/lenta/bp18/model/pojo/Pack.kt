package com.lenta.bp18.model.pojo

import com.lenta.shared.fmp.resources.dao_ext.DictElement


data class Pack(
        val material: String, // sap-код товара
        val materialOsn: String, // sap-код полуфабриката
        val code: String,
        val order: String,
        val quantity: Double,
        val isDefOut: Boolean = false,
        val category: DictElement? = null,
        val defect: DictElement? = null
)