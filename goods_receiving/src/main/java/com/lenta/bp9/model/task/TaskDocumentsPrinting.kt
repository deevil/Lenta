package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

//Таблица 73 ZSGRZ_DOC_PRINT  Структура строки списка документов для печати
data class TaskDocumentsPrinting (
        val appConditionsOutputDoc: String,
        val outputTypeDoc: String,
        val name: String,
        val productDocNumber: String,
        val objectKey: String
) {

    companion object {
        fun from(restData: TaskDocumentsPrintingRestInfo): TaskDocumentsPrinting {
            return TaskDocumentsPrinting(
                    appConditionsOutputDoc = restData.appConditionsOutputDoc,
                    outputTypeDoc = restData.outputTypeDoc,
                    name = restData.name,
                    productDocNumber = restData.productDocNumber,
                    objectKey = restData.objectKey
            )
        }
    }
}

data class TaskDocumentsPrintingRestInfo(
        @SerializedName("KAPPL")
        val appConditionsOutputDoc: String, //Приложение для условий Выходн. Документов
        @SerializedName("KSCHL")
        val outputTypeDoc: String, //Вид выходного документа
        @SerializedName("VTEXT")
        val name: String, //Название
        @SerializedName("MBLNR")
        val productDocNumber: String, //Номер документа товара
        @SerializedName("OBJKY")
        val objectKey: String //Ключ объекта
)