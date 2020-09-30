package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName
import com.lenta.bp12.model.BlockType
import com.lenta.bp12.model.pojo.Block
import com.lenta.bp12.model.pojo.ReturnReason
import com.lenta.bp12.model.pojo.TaskType
import com.lenta.bp12.model.pojo.open_task.TaskOpen
import com.lenta.bp12.platform.extention.getControlTypes
import com.lenta.shared.utilities.extentions.isSapTrue

/**
 * Задание приходящее с сервера, при работе с заданиями
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
        /** Флаг – задание вет. контроля */
        @SerializedName("IS_VET")
        val isVet: String?,
        /** Флаг – алкогольное задание */
        @SerializedName("IS_ALCO")
        val isAlco: String?,
        /** Флаг – маркированное задание */
        @SerializedName("IS_MARK")
        val isMark: String?,
        /** Флаг – задание вет. контроля */
        @SerializedName("IS_USUAL")
        val isUsual: String?,
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
) {
        fun convertToTaskOpen(type: TaskType?, reason: ReturnReason?) : TaskOpen {
                return TaskOpen(
                        number = number.orEmpty(),
                        name = name.orEmpty(),
                        type = type,
                        block = Block(
                                type = BlockType.from(blockType.orEmpty()),
                                user = blockUser.orEmpty(),
                                ip = blockIp.orEmpty()
                        ),
                        storage = storage.orEmpty(),
                        controlTypes = getControlTypes(),
                        provider = ProviderInfo(
                                code = providerCode.orEmpty(),
                                name = providerName.orEmpty()
                        ),
                        reason = reason,
                        comment = comment.orEmpty(),
                        section = section.orEmpty(),
                        goodType = goodType.orEmpty(),
                        purchaseGroup = purchaseGroup.orEmpty(),
                        goodGroup = goodType.orEmpty(),
                        numberOfGoods = quantity?.toIntOrNull() ?: 0,
                        isStrict = isStrict.isSapTrue(),
                        isFinished = !isNotFinish.isSapTrue(),
                        wholesaleBuyer = kunnrName
                )
        }
}