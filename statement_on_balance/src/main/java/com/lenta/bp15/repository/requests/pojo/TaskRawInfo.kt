package com.lenta.bp15.repository.requests.pojo

import com.google.gson.annotations.SerializedName

data class TaskRawInfo(
        /** Номер задания */
        @SerializedName("TASK_NUM")
        val number: String,
        /** Тип задания */
        @SerializedName("TASK_TYPE")
        val type: String,
        /** Текст первой строки */
        @SerializedName("TEXT1")
        val firstLine: String,
        /** Текст второй строки */
        @SerializedName("TEXT2")
        val secondLine: String,
        /** Текст заголовка задания */
        @SerializedName("TEXT3")
        val title: String,
        /** Название задания */
        @SerializedName("DESCR")
        val description: String,
        /** Тип блокировки: 1 - своя, 2 - чужая */
        @SerializedName("BLOCK_TYPE")
        val lockType: String,
        /** Логин сотрудника, под которым выполнена блокировка задания */
        @SerializedName("LOCK_USER")
        val lockUser: String,
        /** IP адрес ТСД, под которым была выполнена блокировка */
        @SerializedName("LOCK_IP")
        val lockIp: String,
        /** Обработка задания не закончена */
        @SerializedName("NOT_FINISH")
        val isNotFinish: String,
        /** Количество позиций в задании */
        @SerializedName("QNT_POS")
        val goodsQuantity: String,
        /** Количество необработанных марок в задании */
        @SerializedName("QNT_MARK")
        val marksQuantity: String,
        /** Текстовый комментарий */
        @SerializedName("COMMENT")
        val comment: String
)