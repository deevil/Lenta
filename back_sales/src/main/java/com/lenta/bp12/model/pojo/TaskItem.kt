package com.lenta.bp12.model.pojo

import com.google.gson.annotations.SerializedName

data class TaskItem(
        /** Номер задания */
        @SerializedName("TASK_NUM")
        var number: String,
        /** Наименование задания */
        @SerializedName("DESCR")
        var description: String,
        /** Тип задания */
        @SerializedName("TASK_TYPE")
        var type: String,
        /** Склад отправитель */
        @SerializedName("LGORT_SRC")
        var storage: String,
        /** Индикатор: Строгий список */
        @SerializedName("IS_STRICT")
        var isStrict: String,
        /** Тип блокировки: 1 - своя, 2 - чужая */
        @SerializedName("BLOCK_TYPE")
        var blockType: String,
        /** Логин сотрудника, под которым выполнена блокировка задания */
        @SerializedName("LOCK_USER")
        var blockUser: String,
        /** IP сотрудника под которым выполнена блокировка задания */
        @SerializedName("LOCK_IP")
        var blockIp: String,
        /** Обработка задания не закончена */
        @SerializedName("NOT_FINISH")
        var isNotFinish: String,
        /** Гис-контроль задания */
        @SerializedName("TASK_CNTRL")
        var gisControlType: String,
        /** Текстовый комментарий */
        @SerializedName("COMMENT")
        var comment: String,
        /** Код поставщика */
        @SerializedName("LIFNR")
        var supplierCode: String,
        /** Количество позиций */
        @SerializedName("QNT_POS")
        var quantity: String,
        /** Код причины возврата */
        @SerializedName("REASONE_CODE")
        var reasonCode: String
)