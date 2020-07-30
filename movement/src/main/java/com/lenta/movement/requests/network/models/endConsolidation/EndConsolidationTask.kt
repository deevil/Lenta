package com.lenta.movement.requests.network.models.endConsolidation

import com.google.gson.annotations.SerializedName
import com.lenta.movement.models.MovementType
import com.lenta.movement.models.TaskType
import com.lenta.movement.requests.network.models.Taskable

data class EndConsolidationTask(
        /**Номер задания на перемещение*/
        @SerializedName("TASK_NUM")
        override val taskNumber: String,

        /**Название задания*/
        @SerializedName("DESCR")
        override val description: String,

        /**Тип задания на перемещение*/
        @SerializedName("TASK_TYPE")
        override val taskType: TaskType,

        /**Тип перемещения (сценарий перемещения)*/
        @SerializedName("TYPE_MVM")
        override val movementType: MovementType,

        /**Натуральное число*/
        @SerializedName("QNT_POS")
        override val quantityPosition: String,

        /**Склад комплектации*/
        @SerializedName("LGORT_SRC")
        override val lgortSrc: String,

        /**Склад отгрузки*/
        @SerializedName("LGORT_TGT")
        override val lgortTarget: String,

        /**Предп*/
        @SerializedName("WERKS_DSTNTN")
        override val werksDstntnt: String,

        /** Тип блокировки (своя/чужая) */
        @SerializedName("BLOCK_TYPE")
        override val blockType: String,

        /** Имя пользователя */
        @SerializedName("LOCK_USER")
        val lockUser: String,

        /** IP адрес ТСД */
        @SerializedName("LOCK_IP")
        val lockIp: String,

        /** Общий флаг */
        @SerializedName("NOT_FINISH")
        override val notFinish: String,

        /** Общий флаг */
        @SerializedName("IS_CONS")
        override val isCons: String,

        /** Статус задания */
        @SerializedName("CUR_STAT")
        override val currentStatusCode: String,

        /** Текст статус задания */
        @SerializedName("CUR_STAT_TEXT")
        override val currentStatusText: String,

        /** Текст следующего статуса задания */
        @SerializedName("NEXT_STAT_TEXT")
        override val nextStatusText: String,

        /** УТЗ ТСД. Комментарий при смене статуса */
        @SerializedName("TASK_COMMENT")
        override val taskComment: String,

        /** Поле типа DATS */
        @SerializedName("DATE_SHIP")
        override val dateShip: String
) : Taskable