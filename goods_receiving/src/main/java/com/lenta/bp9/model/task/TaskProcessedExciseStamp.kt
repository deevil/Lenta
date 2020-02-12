package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName
import com.lenta.shared.models.core.ExciseStamp

class TaskProcessedExciseStamp(materialNumber: String, //Номер товара
                      code: String, //Код акцизной марки
                      val processingUnitNumber: String,
                      val reasonRejection: String,
                      val isScan: Boolean,
                      val boxNumber: String,
                      val packNumber: String,
                      val isMSC: Boolean,
                      val organizationCodeEGAIS: String,
                      val bottlingDate: String,
                      val isUnknown: Boolean ) : ExciseStamp(materialNumber, code)
{

    companion object {
        fun from(restData: TaskProcessedExciseStampRestData): TaskProcessedExciseStamp {
            return TaskProcessedExciseStamp(
                    materialNumber = restData.materialNumber,
                    code = restData.code,
                    processingUnitNumber = restData.processingUnitNumber,
                    reasonRejection = restData.reasonRejection,
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

data class TaskProcessedExciseStampRestData(
        @SerializedName("EXIDV") //Номер ЕО
        val processingUnitNumber: String,
        @SerializedName("MATNR") //Номер товара
        val materialNumber: String,
        @SerializedName("MARK_NUM") //Код акцизной марки
        val code: String,
        @SerializedName("GRUND") //Причина отклонения при ППП
        var reasonRejection: String,
        @SerializedName("IS_SCAN")
        var isScan: String,
        @SerializedName("BOX_NUM") //Номер коробки
        val boxNumber: String,
        @SerializedName("PACK_NUM") //Номер дополнительной упаковки
        val packNumber: String,
        @SerializedName("IS_MSC") //Общий флаг
        var isMSC: String,
        @SerializedName("ZPROD") //ЕГАИС Код организации
        val organizationCodeEGAIS: String,
        @SerializedName("BOTT_MARK") //УТЗ ТСД: Дата розлива
        val bottlingDate: String,
        @SerializedName("IS_UNKNOWN") //Общий флаг
        val isUnknown: String
) {

    companion object {
        fun from(data: TaskProcessedExciseStamp): TaskProcessedExciseStampRestData {
            return TaskProcessedExciseStampRestData(
                    materialNumber = data.materialNumber,
                    code = data.code,
                    processingUnitNumber = data.processingUnitNumber,
                    reasonRejection = data.reasonRejection,
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