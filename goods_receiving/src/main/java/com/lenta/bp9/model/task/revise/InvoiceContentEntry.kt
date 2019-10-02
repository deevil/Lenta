package com.lenta.bp9.model.task.revise

import com.google.gson.annotations.SerializedName
import com.lenta.shared.utilities.extentions.toStringFormatted

//ET_DOC_CHK - Таблица сверки документов по поставке
data class InvoiceContentEntry(
        val matnr: String, //Номер товара
        val orderPositionNumber: String, //Номер позиции заказа (пустой, если товара нет в текущей поставке)
        val supplyPositionNumber: String,  // Номер позиции поставки
        var quantityInOrder: Double,   // Количество в заказе
        val registeredQuantity: Double, // Количество, которое біло оприходовано по этому заказу и этому товару
        val originalQuantity: Double, // Исходное количество позиции поставки
        val units: String, // Единицы измерения
        var isAdded: Boolean,
        var isDeleted: Boolean
) {

    companion object {
        fun from(restData: InvoiceContentEntryRestData): InvoiceContentEntry {
            return InvoiceContentEntry(
                    matnr = restData.matnr,
                    orderPositionNumber = restData.orderPositionNumber,
                    supplyPositionNumber = restData.supplyPositionNumber,
                    quantityInOrder = restData.quantityInOrder.toDouble(),
                    registeredQuantity = restData.registeredQuantity.toDouble(),
                    originalQuantity = restData.originalQuantity.toDouble(),
                    units = restData.units,
                    isAdded = restData.isAdded.isNotEmpty(),
                    isDeleted = restData.isDeleted.isNotEmpty()
            )
        }
    }
}

data class InvoiceContentEntryRestData(
        @SerializedName("MATNR")
        val matnr: String,
        @SerializedName("EBELP")
        val orderPositionNumber: String,
        @SerializedName("VBELP")
        val supplyPositionNumber: String,
        @SerializedName("MENGE")
        var quantityInOrder: String,
        @SerializedName("WEMNG")
        val registeredQuantity: String,
        @SerializedName("ORMNG")
        val originalQuantity: String,
        @SerializedName("BSTME")
        val units: String,
        @SerializedName("FLG_ADD")
        var isAdded: String,
        @SerializedName("FLG_DEL")
        var isDeleted: String) {

    companion object {
        fun from(data: InvoiceContentEntry): InvoiceContentEntryRestData {
            return InvoiceContentEntryRestData(
                    matnr = data.matnr,
                    orderPositionNumber = data.orderPositionNumber,
                    supplyPositionNumber = data.supplyPositionNumber,
                    quantityInOrder = data.quantityInOrder.toStringFormatted(),
                    registeredQuantity = data.registeredQuantity.toStringFormatted(),
                    originalQuantity = data.originalQuantity.toStringFormatted(),
                    units = data.units,
                    isAdded = if (data.isAdded) "X" else "",
                    isDeleted = if (data.isDeleted) "X" else ""
            )
        }
    }
}