package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName
import com.lenta.shared.models.core.ExciseStamp

class TaskExciseStampInfo(materialNumber: String, //Номер товара
                      code: String, //Код акцизной марки
                      val processingUnitNumber: String,
                      val batchNumber: String,
                      val boxNumber: String,
                      val setMaterialNumber: String,
                      val organizationCodeEGAIS: String,
                      val bottlingDate: String ) : ExciseStamp(materialNumber, code)
{

    companion object {
        fun from(restData: TaskExciseStampInfoRestData): TaskExciseStampInfo {
            return TaskExciseStampInfo(
                    materialNumber = restData.materialNumber,
                    code = restData.code,
                    processingUnitNumber = restData.processingUnitNumber,
                    batchNumber = restData.batchNumber,
                    boxNumber = restData.boxNumber,
                    setMaterialNumber = restData.setMaterialNumber,
                    organizationCodeEGAIS = restData.organizationCodeEGAIS,
                    bottlingDate = restData.bottlingDate
            )
        }
    }
}

data class TaskExciseStampInfoRestData(
        @SerializedName("EXIDV") //Номер ЕО
        val processingUnitNumber: String,
        @SerializedName("MATNR") //Номер товара
        val materialNumber: String,
        @SerializedName("ZCHARG") //Номер партии
        val batchNumber: String,
        @SerializedName("BOX_NUM") //Номер коробки
        val boxNumber: String,
        @SerializedName("MARK_NUM") //Код акцизной марки
        val code: String,
        @SerializedName("MATNR_OSN") // Номер набора
        val setMaterialNumber: String,
        @SerializedName("ZPROD") //ЕГАИС Код организации
        val organizationCodeEGAIS: String,
        @SerializedName("BOTT_MARK") //УТЗ ТСД: Дата розлива
        val bottlingDate: String
) {

    companion object {
        fun from(data: TaskExciseStampInfo): TaskExciseStampInfoRestData {
            return TaskExciseStampInfoRestData(
                    materialNumber = data.materialNumber,
                    code = data.code,
                    processingUnitNumber = data.processingUnitNumber,
                    batchNumber = data.batchNumber,
                    boxNumber = data.boxNumber,
                    setMaterialNumber = data.setMaterialNumber,
                    organizationCodeEGAIS = data.organizationCodeEGAIS,
                    bottlingDate = data.bottlingDate
            )
        }
    }
}