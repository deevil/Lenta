package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

//ET_MATNR_ABTNR Таблица Секция\представитель  (ZSGRZ_TASK_PERNR_EXCH)
data class TaskSectionProducts(
        val sectionNumber: String, //Номер секции
        val materialNumber: String //Номер товара
) {
        companion object {
                fun from(restData: TaskSectionProductsRestData): TaskSectionProducts {
                        return TaskSectionProducts(
                                sectionNumber = restData.sectionNumber,
                                materialNumber = restData.materialNumber
                        )
                }
        }
}

data class TaskSectionProductsRestData(
        @SerializedName("ABTNR")
        val sectionNumber: String, //Номер секции
        @SerializedName("MATNR")
        val materialNumber: String //Номер товара
)