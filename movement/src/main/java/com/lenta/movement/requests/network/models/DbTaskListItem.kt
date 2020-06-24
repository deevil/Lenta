package com.lenta.movement.requests.network.models

import com.google.gson.annotations.SerializedName
import com.lenta.movement.models.MovementType
import com.lenta.movement.models.TaskType

data class DbTaskListItem(
        /**Номер задания на перемещение*/
        @SerializedName("TASK_NUM")
        val taskNumber: String,

        /**Название задания*/
        @SerializedName("DESCR")
        val description: String,

        /**Тип задания на перемещение*/
        @SerializedName("TASK_TYPE")
        val taskType: TaskType,

        /**Тип перемещения (сценарий перемещения)*/
        @SerializedName("TYPE_MVM")
        val movementType: MovementType,

        /**Натуральное число*/
        @SerializedName("QNT_POS")
        val quantityPosition: String,

        /**Склад комплектации*/
        @SerializedName("LGORT_SRC")
        val lgortSrc: String,

        /**Склад отгрузки*/
        @SerializedName("LGORT_TGT")
        val lgortTarget: String,

        /**Предп*/
        @SerializedName("WERKS_DSTNTN")
        val werksDstntnt: String,

        /** Тип блокировки (своя/чужая) */
        @SerializedName("BLOCK_TYPE")
        val blockType: String,

        /** Имя пользователя */
        @SerializedName("LOCK_USER")
        val lockUser: String,

        /** IP адрес ТСД */
        @SerializedName("LOCK_IP")
        val lockIp: String,

        /** Общий флаг */
        @SerializedName("NOT_FINISH")
        val notFinish: String,

        /** Общий флаг */
        @SerializedName("IS_CONS")
        val isCons: String,

        /** Тип ГИС-контроля */
        @SerializedName("TASK_CNTRL")
        val taskCntrl: String,

        @SerializedName("TASK_COMMENT")
        val taskComment: String,

        @SerializedName("DATE_SHIP")
        val dateShip: String,

        @SerializedName("CUR_STAT")
        val currentStatusCode: String,

        @SerializedName("CUR_STAT_TEXT")
        val currentStatusText: String,

        @SerializedName("NEXT_STAT_TEXT")
        val nextStatusText: String,

        @SerializedName("ERROR_TEXT")
        val errorText: String
)