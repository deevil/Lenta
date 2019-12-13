package com.lenta.bp16.model.pojo


data class Pack(
        val material: String, // sap-код товара
        val materialOsn: String, // sap-код полуфабриката
        val code: String,
        val quantity: Double
) {

    fun getShortPackNumber(): String {
        var number = code
        while (number.isNotEmpty() && number.startsWith("0")) {
            number = number.substring(1)
        }

        return number
    }

}