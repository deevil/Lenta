package com.lenta.bp9.model.task.revise

import com.google.gson.annotations.SerializedName
import com.lenta.shared.models.core.ExciseStamp

//ET_MARK_DIFF Таблица обработанных марок задания
class TaskExciseStampDiscrepancies(materialNumber: String, //Номер товара
                      code: String, //Код акцизной марки
                      val processingUnitNumber: String,
                      val typeDiscrepancies: String,
                      val isScan: Boolean,
                      val boxNumber: String,
                      val packNumber: String,
                      val isMSC: Boolean,
                      val organizationCodeEGAIS: String,
                      val bottlingDate: String,
                      val isUnknown: Boolean) : ExciseStamp(materialNumber, code)
{

    companion object {
        fun from(restData: TaskExciseStampDiscrepanciesRestData): TaskExciseStampDiscrepancies {
            return TaskExciseStampDiscrepancies(
                    materialNumber = restData.materialNumber,
                    code = restData.code,
                    processingUnitNumber = restData.processingUnitNumber,
                    typeDiscrepancies = restData.typeDiscrepancies,
                    isScan = restData.isScan.isNotEmpty(),
                    boxNumber = restData.boxNumber,
                    packNumber = restData.packNumber,
                    isMSC = restData.isMSC.isNotEmpty(),
                    organizationCodeEGAIS = restData.organizationCodeEGAIS,
                    bottlingDate = restData.bottlingDate,
                    isUnknown = restData.isUnknown.isNotEmpty()
            )
        }
    }
}

data class TaskExciseStampDiscrepanciesRestData(
        @SerializedName("EXIDV") //Номер ЕО
        val processingUnitNumber: String,
        @SerializedName("MATNR") //Номер товара
        val materialNumber: String,
        @SerializedName("MARK_NUM") //Код акцизной марки
        val code: String,
        @SerializedName("GRUND") //тип расхождения
        val typeDiscrepancies: String,
        @SerializedName("IS_SCAN")
        val isScan: String,
        @SerializedName("BOX_NUM") //Номер коробки
        val boxNumber: String,
        @SerializedName("PACK_NUM") //Номер дополнительной упаковки
        val packNumber: String,
        @SerializedName("IS_MSC")
        val isMSC: String,
        @SerializedName("ZPROD") //ЕГАИС Код организации
        val organizationCodeEGAIS: String,
        @SerializedName("BOTT_MARK") //УТЗ ТСД: Дата розлива
        val bottlingDate: String,
        @SerializedName("IS_UNKNOWN")
        val isUnknown: String
) {

    companion object {
        fun from(data: TaskExciseStampDiscrepancies): TaskExciseStampDiscrepanciesRestData {
            return TaskExciseStampDiscrepanciesRestData(
                    materialNumber = data.materialNumber,
                    code = data.code,
                    processingUnitNumber = data.processingUnitNumber,
                    typeDiscrepancies = data.typeDiscrepancies,
                    isScan = if (data.isScan) "X" else "",
                    boxNumber = data.boxNumber,
                    packNumber = data.packNumber,
                    isMSC = if (data.isMSC) "X" else "",
                    organizationCodeEGAIS = data.organizationCodeEGAIS,
                    bottlingDate = data.bottlingDate,
                    isUnknown = if (data.isUnknown) "X" else ""
            )
        }
    }
}