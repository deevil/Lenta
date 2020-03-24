package com.lenta.bp12.request

import com.google.gson.annotations.SerializedName
import com.lenta.bp12.model.pojo.TaskCreate
import com.lenta.bp12.request.pojo.SentTaskInfo
import com.lenta.bp12.request.pojo.GoodInfo
import com.lenta.bp12.request.pojo.MarkInfo
import com.lenta.bp12.request.pojo.PartInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.toSapBooleanString
import javax.inject.Inject

class SendTaskDataNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<SendTaskDataResult, TaskData> {

    override suspend fun run(taskData: TaskData): Either<Failure, SendTaskDataResult> {

        val goods = mutableListOf<GoodInfo>()
        val marks = mutableListOf<MarkInfo>()
        val parts = mutableListOf<PartInfo>()

        taskData.task.goods.forEach { good ->
            goods.add(
                    GoodInfo(
                            material = good.material,
                            providerCode = good.provider?.code ?: "Not found!",
                            quantity = good.quantity.dropZeros(),
                            isCounted = good.isCounted.toSapBooleanString(),
                            isDeleted = good.isDelete.toSapBooleanString(),
                            unitsCode = good.units.code
                    )
            )

            good.marks.map { mark ->
                marks.add(
                        MarkInfo(
                                material = good.material,
                                markNumber = mark.markNumber,
                                boxNumber = mark.boxNumber,
                                isBadMark = mark.isBadMark.toSapBooleanString(),
                                providerCode = mark.providerCode
                        )
                )
            }

            good.parts.map { part ->
                parts.add(
                        PartInfo(
                                material = good.material,
                                producer = part.producer,
                                productionDate = part.productionDate,
                                unitsCode = part.units.code,
                                quantity = part.quantity.dropZeros(),
                                partNumber = part.partNumber,
                                providerCode = part.providerCode
                        )
                )
            }
        }

        return fmpRequestsHelper.restRequest("ZMP_UTZ_BKS_04_V001",
                SendTaskDataParams(
                        deviceIp = taskData.deviceIp,
                        taskNumber = taskData.task.number,
                        userNumber = taskData.userNumber,
                        taskName = taskData.task.name,
                        taskType = taskData.task.type.type,
                        tkNumber = taskData.tkNumber,
                        storage = taskData.task.storage,
                        reasonCode = taskData.task.reason.code,
                        isNotFinish = (!taskData.task.isFinish).toSapBooleanString(),
                        goods = goods,
                        marks = marks,
                        parts = parts
                ), SendTaskDataStatus::class.java)
    }

}

data class TaskData(
        val deviceIp: String,
        val tkNumber: String,
        val userNumber: String,
        val task: TaskCreate
)

data class SendTaskDataParams(
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val deviceIp: String,
        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val userNumber: String,
        /** Название задания */
        @SerializedName("IV_DESCR")
        val taskName: String,
        /** Тип задания */
        @SerializedName("IV_TYPE")
        val taskType: String,
        /** Номер ТК */
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        /** Склад отправитель */
        @SerializedName("IV_LGORT_SRC")
        val storage: String,
        /** Код причины возврата */
        @SerializedName("IV_REASONE_CODE")
        val reasonCode: String,
        /** Обработка задания не закончена */
        @SerializedName("IV_NOT_FINISH")
        val isNotFinish: String,
        /** Таблица состава задания */
        @SerializedName("IT_TASK_POS")
        val goods: List<GoodInfo>,
        /** Таблица марок задания */
        @SerializedName("IT_TASK_MARK")
        val marks: List<MarkInfo>,
        /** Таблица партий */
        @SerializedName("IT_TASK_PARTS")
        val parts: List<PartInfo>
)

class SendTaskDataStatus : ObjectRawStatus<SendTaskDataResult>()

data class SendTaskDataResult(
        /** Список созданных заданий */
        @SerializedName("ET_TASK_LIST")
        val sentTasks: List<SentTaskInfo>,
        /** Код возврата */
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse