package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

//ET_TASK_PERNR Таблица Секция\представитель (ZSGRZ_TASK_PERNR_EXCH)
data class TaskSectionInfo(
    val sectionNumber: String, //Номер секции
    val sectionName: String, //Название секции
    val personnelNumber: String, //Табельный номер представителя
    val employeeName: String, //Отформатированное имя сотрудника или кандидата на должность
    val quantitySectionProducts: String, //Количество товаров секции
    val dateTransferSection: String, //Дата передачи в секцию
    val timeTransferSection: String, //Время передачи в секцию
    val isNotEdit: Boolean //Запрет редактирования
) {
        companion object {
                fun from(restData: TaskSectionRestData): TaskSectionInfo {
                        return TaskSectionInfo(
                                sectionNumber = restData.sectionNumber,
                                sectionName = restData.sectionName,
                                personnelNumber = if (restData.personnelNumber == "00000000") {""} else {restData.personnelNumber},
                                employeeName = restData.employeeName,
                                quantitySectionProducts = restData.quantitySectionProducts,
                                dateTransferSection = restData.dateTransferSection,
                                timeTransferSection = restData.timeTransferSection,
                                isNotEdit = restData.isNotEdit.isNotEmpty()
                        )
                }
        }
}

data class TaskSectionRestData(
        @SerializedName("ABTNR")
        val sectionNumber: String, //Номер секции
        @SerializedName("VTEXT")
        val sectionName: String, //Название секции
        @SerializedName("PERNR")
        val personnelNumber: String, //Табельный номер представителя
        @SerializedName("EMNAM")
        val employeeName: String, //Отформатированное имя сотрудника или кандидата на должность
        @SerializedName("QNT_POS")
        val quantitySectionProducts: String, //Количество товаров секции
        @SerializedName("TRANSF_DATE")
        val dateTransferSection: String, //Дата передачи в секцию
        @SerializedName("TRANSF_TIME")
        val timeTransferSection: String, //Время передачи в секцию
        @SerializedName("NOT_EDIT")
        val isNotEdit: String //Запрет редактирования
) {

        companion object {
                fun from(data: TaskSectionInfo): TaskSectionRestData {
                        return TaskSectionRestData(
                                sectionNumber = data.sectionNumber,
                                sectionName = data.sectionName,
                                personnelNumber = data.personnelNumber,
                                employeeName = data.employeeName,
                                quantitySectionProducts = data.quantitySectionProducts,
                                dateTransferSection = data.dateTransferSection,
                                timeTransferSection = data.timeTransferSection,
                                isNotEdit = if (data.isNotEdit) "X" else ""
                        )
                }
        }
}