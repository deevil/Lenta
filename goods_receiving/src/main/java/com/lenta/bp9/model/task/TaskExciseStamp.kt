package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName
import com.lenta.shared.models.core.ExciseStamp

class TaskExciseStamp(materialNumber: String, //Номер товара
                      code: String, //Код акцизной марки
                      val processingUnitNumber: String,
                      val batchNumber: String,
                      val boxNumber: String,
                      val materialNumberSet: String,
                      val organizationCodeEGAIS: String,
                      val bottlingDate: String ) : ExciseStamp(materialNumber, code)
{

    companion object {
        fun from(restData: TaskExciseStampRestData): TaskExciseStamp {
            return TaskExciseStamp(
                    materialNumber = restData.materialNumber,
                    code = restData.code,
                    processingUnitNumber = restData.processingUnitNumber,
                    batchNumber = restData.batchNumber,
                    boxNumber = restData.boxNumber,
                    materialNumberSet = restData.materialNumberSet,
                    organizationCodeEGAIS = restData.organizationCodeEGAIS,
                    bottlingDate = restData.bottlingDate
            )
        }
    }
}

data class TaskExciseStampRestData(
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
        val materialNumberSet: String,
        @SerializedName("ZPROD") //ЕГАИС Код организации
        val organizationCodeEGAIS: String,
        @SerializedName("BOTT_MARK") //УТЗ ТСД: Дата розлива
        val bottlingDate: String
) {

    companion object {
        fun from(data: TaskExciseStamp): TaskExciseStampRestData {
            return TaskExciseStampRestData(
                    materialNumber = data.materialNumber,
                    code = data.code,
                    processingUnitNumber = data.processingUnitNumber,
                    batchNumber = data.batchNumber,
                    boxNumber = data.boxNumber,
                    materialNumberSet = data.materialNumberSet,
                    organizationCodeEGAIS = data.organizationCodeEGAIS,
                    bottlingDate = data.bottlingDate
            )
        }
    }
}