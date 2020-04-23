package com.lenta.bp16.model.pojo


data class Raw(
        val material: String, // sap-код товара
        val materialOsn: String, // sap-код полуфабриката
        val order: String,
        val name: String,
        val planned: Double,
        val isWasDef: Boolean
) {
}