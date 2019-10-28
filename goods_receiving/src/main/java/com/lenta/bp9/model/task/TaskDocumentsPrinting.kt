package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

//Таблица 73 ZSGRZ_DOC_PRINT  Структура строки списка документов для печати
data class TaskDocumentsPrinting (
        val appConditionsOutputDoc: String,   //Приложение для условий Выходн. Документов
        val outputTypeDoc: String,   //Вид выходного документа
        var name: String    //Название
) {

    companion object {
        fun from(restData: TaskDocumentsPrintingRestInfo): TaskDocumentsPrinting {
            return TaskDocumentsPrinting(appConditionsOutputDoc = restData.appConditionsOutputDoc,
                    outputTypeDoc = restData.outputTypeDoc,
                    name = restData.name
            )
        }
    }
}

data class TaskDocumentsPrintingRestInfo(
        @SerializedName("KAPPL")
        val appConditionsOutputDoc: String,
        @SerializedName("KSCHL")
        val outputTypeDoc: String,
        @SerializedName("VTEXT")
        val name: String
)