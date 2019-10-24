package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

//ET_TASK_PERNR Таблица Секция\представитель (ZSGRZ_TASK_PERNR_EXCH)
data class TaskSectionInfo(
    val sectionNumber: String, //Номер секции
    val personnelNumber: String, //Табельный номер представителя
    val quantitySectionProducts: String, //Количество товаров секции
    val dateTransferSection: String, //Дата передачи в секцию
    val timeTransferSection: String, //Время передачи в секцию
    val isNotEdit: Boolean //Запрет редактирования
) {
        companion object {
                fun from(restData: TaskSectionRestData): TaskSectionInfo {
                        return TaskSectionInfo(
                                sectionNumber = restData.sectionNumber,
                                personnelNumber = restData.personnelNumber,
                                quantitySectionProducts = restData.quantitySectionProducts,
                                dateTransferSection = restData.dateTransferSection,
                                timeTransferSection = restData.timeTransferSection,
                                isNotEdit = restData.notEdit.isNotEmpty()
                        )
                }
        }
}

data class TaskSectionRestData(
        @SerializedName("ABTNR")
        val sectionNumber: String, //Номер секции
        @SerializedName("PERNR")
        val personnelNumber: String, //Табельный номер представителя
        @SerializedName("QNT_POS")
        val quantitySectionProducts: String, //Количество товаров секции
        @SerializedName("TRANSF_DATE")
        val dateTransferSection: String, //Дата передачи в секцию
        @SerializedName("TRANSF_TIME")
        val timeTransferSection: String, //Время передачи в секцию
        @SerializedName("NOT_EDIT")
        val notEdit: String //Запрет редактирования
)