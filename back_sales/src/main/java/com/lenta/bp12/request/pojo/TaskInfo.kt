package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

/**
 * @see com.lenta.bp12.request.TaskListNetRequest
 * */
data class TaskInfo(
        /** Номер задания */
        @SerializedName("TASK_NUM")
        val number: String?,
        /** Наименование задания */
        @SerializedName("DESCR")
        val name: String?,
        /** Тип задания */
        @SerializedName("TASK_TYPE")
        val typeCode: String?,
        /** Склад отправитель */
        @SerializedName("LGORT_SRC")
        val storage: String?,
        /** Индикатор: Строгий список */
        @SerializedName("IS_STRICT")
        val isStrict: String?,
        /** Тип блокировки: 1 - своя, 2 - чужая */
        @SerializedName("BLOCK_TYPE")
        val blockType: String?,
        /** Логин сотрудника, под которым выполнена блокировка задания */
        @SerializedName("LOCK_USER")
        val blockUser: String?,
        /** IP сотрудника под которым выполнена блокировка задания */
        @SerializedName("LOCK_IP")
        val blockIp: String?,
        /** Обработка задания не закончена */
        @SerializedName("NOT_FINISH")
        val isNotFinish: String?,
        /** Гис-контроль задания */
        @SerializedName("TASK_CNTRL")
        val control: String?,
        /** Текстовый комментарий */
        @SerializedName("TASK_COMMENT")
        val comment: String?,
        /** Код поставщика */
        @SerializedName("LIFNR")
        val providerCode: String?,
        /** Наименование поставщика */
        @SerializedName("LIFNR_NAME")
        val providerName: String?,
        /** Количество позиций */
        @SerializedName("QNT_POS")
        val quantity: String?,
        /** Код причины возврата */
        @SerializedName("REASONE_CODE")
        val reasonCode: String?,
        /** Секция */
        @SerializedName("ABTNR")
        val section: String?,
        /** Группа закупок */
        @SerializedName("EKGRP")
        val purchaseGroup: String?,
        /** Вид товара */
        @SerializedName("MTART")
        val goodType: String?,
        /** Код клиента */
        @SerializedName("KUNNR")
        val kunnr: String?,
        /** УТЗ ТСД, Наименование клиента */
        @SerializedName("KUNNR_NAME")
        val kunnrName: String?
)