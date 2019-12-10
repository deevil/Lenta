package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//ET_VET_DIFF Таблица расхождений по вет.товарам
data class TaskMercuryInfo(
        val materialNumber: String,
        val vetDocumentID: String, //ID ветеринарно-сопроводительного документа
        val volume: Double, //Объём груза
        val uom: Uom, //Продажная ЕИ
        val typeDiscrepancies: String, //Тип расхождения (Причина отклонения при ППП)
        val numberDiscrepancies: Double, //Количество расхождения в ПЕИ (Отклонение от фактически поставленного количества в ПЕИ)
        val productionDate: String,	//дата производства
        val manufacturer: String,	//производитель
        val productionDateTo: String	//Поле типа DATS

) {

    companion object {
        suspend fun from(hyperHive: HyperHive, restData: TaskMercuryInfoRestData): TaskMercuryInfo {
            return withContext(Dispatchers.IO) {
                val zmpUtz07V001: ZmpUtz07V001 by lazy {
                    ZmpUtz07V001(hyperHive)
                }
                val uomInfo = zmpUtz07V001.getUomInfo(restData.unit)
                return@withContext TaskMercuryInfo(
                        materialNumber = restData.materialNumber,
                        vetDocumentID = restData.vetDocumentID,
                        volume = restData.volume.toDouble(),
                        uom = Uom(code = uomInfo?.uom ?: "", name = uomInfo?.name ?: ""),
                        typeDiscrepancies = restData.typeDiscrepancies,
                        numberDiscrepancies = restData.numberDiscrepancies.toDouble(),
                        productionDate = restData.productionDates,
                        manufacturer = restData.manufacturers,
                        productionDateTo = restData.productionDateTo
                )
            }

        }
    }
}

data class TaskMercuryInfoRestData(
        @SerializedName("MATNR")
        val materialNumber: String,
        @SerializedName("VSDID")
        val vetDocumentID: String,
        @SerializedName("VSDVOLUME")
        val volume: String,
        @SerializedName("VRKME")
        val unit: String,
        @SerializedName("GRUND")
        val typeDiscrepancies: String,
        @SerializedName("LFIMG_DIFF")
        val numberDiscrepancies: String,
        @SerializedName("PRODDATE")
        val productionDates: String,
        @SerializedName("PROD_NAME")
        val manufacturers: String,
        @SerializedName("PRODDATE_TO")
        val productionDateTo: String
) {

    companion object {
        fun from(data: TaskMercuryInfo?): TaskMercuryInfoRestData? {
            return if (data == null) null else {
                TaskMercuryInfoRestData(
                        materialNumber= data.materialNumber,
                        vetDocumentID = data.vetDocumentID,
                        volume = data.volume.toString(),
                        unit = data.uom.code,
                        typeDiscrepancies = data.typeDiscrepancies,
                        numberDiscrepancies = data.numberDiscrepancies.toString(),
                        productionDates = data.productionDate,
                        manufacturers = data.manufacturer,
                        productionDateTo = data.productionDateTo
                )
            }
        }
    }
}