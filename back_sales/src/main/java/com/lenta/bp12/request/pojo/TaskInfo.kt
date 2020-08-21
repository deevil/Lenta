package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class TaskInfo(
        /** Номер задания */
        @SerializedName("TASK_NUM")
        var number: String?,
        /** Наименование задания */
        @SerializedName("DESCR")
        var name: String?,
        /** Тип задания */
        @SerializedName("TASK_TYPE")
        var typeCode: String?,
        /** Склад отправитель */
        @SerializedName("LGORT_SRC")
        var storage: String?,
        /** Индикатор: Строгий список */
        @SerializedName("IS_STRICT")
        var isStrict: String?,
        /** Тип блокировки: 1 - своя, 2 - чужая */
        @SerializedName("BLOCK_TYPE")
        var blockType: String?,
        /** Логин сотрудника, под которым выполнена блокировка задания */
        @SerializedName("LOCK_USER")
        var blockUser: String?,
        /** IP сотрудника под которым выполнена блокировка задания */
        @SerializedName("LOCK_IP")
        var blockIp: String?,
        /** Обработка задания не закончена */
        @SerializedName("NOT_FINISH")
        var isNotFinish: String?,
        /** Гис-контроль задания */
        @SerializedName("TASK_CNTRL")
        var control: String?,
        /** Текстовый комментарий */
        @SerializedName("TASK_COMMENT")
        var comment: String?,
        /** Код поставщика */
        @SerializedName("LIFNR")
        var providerCode: String?,
        /** Наименование поставщика */
        @SerializedName("LIFNR_NAME")
        var providerName: String?,
        /** Количество позиций */
        @SerializedName("QNT_POS")
        var quantity: String?,
        /** Код причины возврата */
        @SerializedName("REASONE_CODE")
        var reasonCode: String?,
        /** Секция */
        @SerializedName("ABTNR")
        var section: String?,
        /** Группа закупок */
        @SerializedName("EKGRP")
        var purchaseGroup: String?,
        /** Вид товара */
        @SerializedName("MTART")
        var goodType: String?,
        /** Код клиента */
        @SerializedName("KUNNR")
        var kunnr: String?,
        /** УТЗ ТСД, Наименование клиента */
        @SerializedName("KUNNR_NAME")
        var kunnrName: String?
)