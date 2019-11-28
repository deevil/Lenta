package com.lenta.bp9.model.task.revise

import com.google.gson.annotations.SerializedName
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//ET_VET_CHK - Таблица сверки ВСД по товарам
data class ProductVetDocumentRevise(
        val productNumber: String, // Номер товара
        val vetDocumentID: String, // ID ветеринарно- сопроводительного документа
        val attachType: String, // Тип сопоставления ВСД (Меркурий)
        var isAttached: Boolean, // ??? - Общий флаг
        val productName: String, // Название продукции
        val volume: Double, // Объём груза
        val measureUnits: Uom, // Продажная ЕИ
        val originProductName: String, // Наименование продукта
        var isCheck: Boolean, // ??? - Общий флаг
        val attachText: String // ??? - Текст 30 знаков
) {

    companion object {
        suspend fun from(hyperHive: HyperHive, restData: ProductVetDocumentReviseRestData): ProductVetDocumentRevise {
            return withContext(Dispatchers.IO) {
                val zmpUtz07V001: ZmpUtz07V001 by lazy {
                    ZmpUtz07V001(hyperHive)
                }
                val uomInfo = zmpUtz07V001.getUomInfo(restData.measureUnits)
                return@withContext ProductVetDocumentRevise(
                        productNumber = restData.productNumber,
                        vetDocumentID = restData.vetDocumentID,
                        attachType = restData.attachType,
                        isAttached = restData.isAttached.isNotEmpty(),
                        productName = restData.productName,
                        volume = restData.volume.toDouble(),
                        measureUnits = Uom(code = uomInfo?.uom ?: "", name = uomInfo?.name ?: ""),
                        originProductName = restData.originProductName,
                        isCheck = restData.isCheck.isNotEmpty(),
                        attachText = restData.attachText
                )
            }
        }
    }
}

data class ProductVetDocumentReviseRestData(
        @SerializedName("MATNR")
        val productNumber: String,
        @SerializedName("VSDID")
        val vetDocumentID: String,
        @SerializedName("ATTACH_TYPE")
        val attachType: String,
        @SerializedName("IS_ATTACHED")
        val isAttached: String,
        @SerializedName("PRODUCTITEMNAME")
        val productName: String,
        @SerializedName("VSDVOLUME")
        val volume: String,
        @SerializedName("VRKME")
        val measureUnits: String,
        @SerializedName("ORIGINPRODITEMNAME")
        val originProductName: String,
        @SerializedName("FLG_CHECK")
        val isCheck: String,
        @SerializedName("ATTACH_TXT")
        val attachText: String
) {

    companion object {
        fun from(data: ProductVetDocumentRevise): ProductVetDocumentReviseRestData {
            return ProductVetDocumentReviseRestData(
                    productNumber = data.productNumber,
                    vetDocumentID = data.vetDocumentID,
                    attachType = data.attachType,
                    isAttached = if (data.isAttached) "X" else "",
                    productName = data.productName,
                    volume = data.volume.toStringFormatted(),
                    measureUnits = data.measureUnits.code,
                    originProductName = data.originProductName,
                    isCheck = if (data.isCheck) "X" else "",
                    attachText = data.attachText
            )
        }
    }
}