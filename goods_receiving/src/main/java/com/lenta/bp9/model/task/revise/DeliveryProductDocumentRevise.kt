package com.lenta.bp9.model.task.revise

import com.google.gson.annotations.SerializedName
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

//ET_DOC_MATNR_CHK - Таблица для сверки документов поставки
data class DeliveryProductDocumentRevise(
        /** Номер товара (MATNR)*/
        val productNumber: String,
        /** ID Документа (DOC_ID)*/
        val documentID: String,
        /** Название документа (DOC_NAME)*/
        val documentName: String,
        /** (OBLIGATORY)*/
        val isObligatory: Boolean,
        /** (FLG_CHECK)*/
        var isCheck: Boolean,
        /** Тип документа (DOC_TYPE)*/
        val documentType: ProductDocumentType,
        /** УТЗ ТСД: Индикатор: Признак набора (IS_SET)*/
        val isSet: Boolean,
        /** Исходное количество позиции поставки (ORMNG)*/
        val initialCount: Double,
        /** Продажная ЕИ (VRKME)*/
        val measureUnits: Uom
) {
    companion object {
        suspend fun from(hyperHive: HyperHive, restData: DeliveryProductDocumentReviseRestData): DeliveryProductDocumentRevise {
            return withContext(Dispatchers.IO) {
                val zmpUtz07V001: ZmpUtz07V001 by lazy {
                    ZmpUtz07V001(hyperHive)
                }
                val uomInfo = zmpUtz07V001.getUomInfo(restData.measureUnits)
                return@withContext DeliveryProductDocumentRevise(
                        productNumber = restData.productNumber,
                        documentID = restData.documentID,
                        documentName = restData.documentName,
                        isObligatory = restData.isObligatory.isNotEmpty(),
                        isCheck = restData.isCheck.isNotEmpty(),
                        documentType = ProductDocumentType.from(restData.documentType),
                        isSet = restData.isSet.isNotEmpty(),
                        initialCount = restData.initialCount.toDouble(),
                        measureUnits = Uom(code = uomInfo?.uom ?: "", name = uomInfo?.name ?: "")
                )
            }
        }
    }
}

data class DeliveryProductDocumentReviseRestData(
        @SerializedName("MATNR")
        val productNumber: String,
        @SerializedName("DOC_ID")
        val documentID: String,
        @SerializedName("DOC_NAME")
        val documentName: String,
        @SerializedName("OBLIGATORY")
        val isObligatory: String,
        @SerializedName("FLG_CHECK")
        val isCheck: String,
        @SerializedName("DOC_TYPE")
        val documentType: String,
        @SerializedName("IS_SET")
        val isSet: String,
        @SerializedName("ORMNG")
        val initialCount: String,
        @SerializedName("VRKME")
        val measureUnits: String) {

    companion object {
        fun from(data: DeliveryProductDocumentRevise): DeliveryProductDocumentReviseRestData {
            return DeliveryProductDocumentReviseRestData(
                    productNumber = data.productNumber,
                    documentID = data.documentID,
                    documentName = data.documentName,
                    isObligatory = if (data.isObligatory) "X" else "",
                    isCheck = if (data.isCheck) "X" else "",
                    documentType = data.documentType.documentTypeString,
                    isSet = if (data.isSet) "X" else "",
                    initialCount = data.initialCount.toString(),
                    measureUnits = data.measureUnits.code
            )
        }
    }
}

enum class ProductDocumentType(val documentTypeString: String) {
    None(""),
    Simple("0"),
    AlcoRus("3"),
    AlcoImport("4"),
    Mercury("5");

    companion object {
        fun from(documentTypeString: String): ProductDocumentType {
            return when (documentTypeString) {
                "0" -> Simple
                "3" -> AlcoRus
                "4" -> AlcoImport
                "5" -> Mercury
                else -> None
            }
        }
    }
}