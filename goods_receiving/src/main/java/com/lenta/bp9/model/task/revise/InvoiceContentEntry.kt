package com.lenta.bp9.model.task.revise

import com.google.gson.annotations.SerializedName
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//ET_DOC_CHK - Таблица сверки документов по поставке
data class InvoiceContentEntry(
        val materialNumber: String, //Номер товара
        val description: String,
        val orderPositionNumber: String, //Номер позиции заказа (пустой, если товара нет в текущей поставке)
        val supplyPositionNumber: String,  // Номер позиции поставки
        var quantityInOrder: Double,   // Количество в заказе
        val registeredQuantity: Double, // Количество, которое біло оприходовано по этому заказу и этому товару
        val originalQuantity: Double, // Исходное количество позиции поставки
        val uom: Uom, // Единицы измерения
        var isAdded: Boolean,
        var isDeleted: Boolean
) {

    companion object {
        suspend fun from(hyperHive: HyperHive, restData: InvoiceContentEntryRestData): InvoiceContentEntry {
            return withContext(Dispatchers.IO) {
                val zmpUtz07V001: ZmpUtz07V001 by lazy {
                    ZmpUtz07V001(hyperHive)
                }
                val zfmpUtz48V001: ZfmpUtz48V001 by lazy {
                    ZfmpUtz48V001(hyperHive)
                }
                val materialInfo = zfmpUtz48V001.getProductInfoByMaterial(restData.materialNumber)
                val uomInfo = zmpUtz07V001.getUomInfo(restData.units)
                return@withContext InvoiceContentEntry(
                        materialNumber = restData.materialNumber,
                        description = materialInfo?.name ?: "",
                        orderPositionNumber = restData.orderPositionNumber,
                        supplyPositionNumber = restData.supplyPositionNumber,
                        quantityInOrder = restData.quantityInOrder.toDouble(),
                        registeredQuantity = restData.registeredQuantity.toDouble(),
                        originalQuantity = restData.originalQuantity.toDouble(),
                        uom = Uom(code = uomInfo?.uom ?: "", name = uomInfo?.name ?: ""),
                        isAdded = restData.isAdded.isNotEmpty(),
                        isDeleted = restData.isDeleted.isNotEmpty()
                )
            }

        }
    }

    fun getMaterialLastSix(): String {
        return if (materialNumber.length > 6)
            materialNumber.substring(materialNumber.length - 6)
        else
            materialNumber
    }
}

data class InvoiceContentEntryRestData(
        @SerializedName("MATNR")
        val materialNumber: String,
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
                    materialNumber = data.materialNumber,
                    orderPositionNumber = data.orderPositionNumber,
                    supplyPositionNumber = data.supplyPositionNumber,
                    quantityInOrder = data.quantityInOrder.toStringFormatted(),
                    registeredQuantity = data.registeredQuantity.toStringFormatted(),
                    originalQuantity = data.originalQuantity.toStringFormatted(),
                    units = data.uom.code,
                    isAdded = if (data.isAdded) "X" else "",
                    isDeleted = if (data.isDeleted) "X" else ""
            )
        }
    }
}