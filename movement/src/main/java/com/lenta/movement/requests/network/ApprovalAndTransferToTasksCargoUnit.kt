package com.lenta.movement.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.movement.exception.InfoFailure
import com.lenta.movement.models.MovementType
import com.lenta.movement.models.TaskType
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

/** ZMP_UTZ_MVM_08_V001	«Начало консолидации» */
class ApprovalAndTransferToTasksCargoUnit @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<ApprovalAndTransferToTasksCargoUnitResult, ApprovalAndTransferToTasksCargoUnitParams> {

    override suspend fun run(params: ApprovalAndTransferToTasksCargoUnitParams): Either<Failure, ApprovalAndTransferToTasksCargoUnitResult> {
        return fmpRequestsHelper.restRequest(
                resourceName = "ZMP_UTZ_MVM_08_V001",
                data = params,
                clazz = ApprovalAndTransferToTasksCargoUnitStatus::class.java
        ).let {
            if (it is Either.Left) {
                return@let it
            }

            if (it is Either.Right && it.b.retCode != "0") {
                return@let Either.Left(InfoFailure(it.b.errorTxt))
            }

            return@let it
        }
    }

}

data class ApprovalAndTransferToTasksCargoUnitParams(
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val deviceIp: String,

        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,

        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val personnelNumber: String
)

class ApprovalAndTransferToTasksCargoUnitStatus : ObjectRawStatus<ApprovalAndTransferToTasksCargoUnitResult>()

data class ApprovalAndTransferToTasksCargoUnitResult(
        /** Таблица состава задания */
        @SerializedName("ET_TASK_LIST")
        val taskList: List<Task>,

        /** Код возврата */
        @SerializedName("EV_RETCODE")
        val retCode: String,

        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        val errorTxt: String
) {

    data class Task(
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
            val taskCntrl: String
    )

}

