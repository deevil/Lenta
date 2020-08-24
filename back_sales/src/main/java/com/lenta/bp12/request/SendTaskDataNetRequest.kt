package com.lenta.bp12.request

import com.google.gson.annotations.SerializedName
import com.lenta.bp12.request.pojo.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class SendTaskDataNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<SendTaskDataResult, SendTaskDataParams> {

    override suspend fun run(params: SendTaskDataParams): Either<Failure, SendTaskDataResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_BKS_04_V001", params, SendTaskDataStatus::class.java)
    }

}

data class SendTaskDataParams(
        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String = "",
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val deviceIp: String,
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
        val positions: List<PositionInfo>,
        /** Таблица марок задания */
        @SerializedName("IT_TASK_MARK")
        val exciseMarks: List<ExciseMarkInfo>,
        /** Таблица партий */
        @SerializedName("IT_TASK_PARTS")
        val parts: List<PartInfo>,
        /** Таблица корзин */
        @SerializedName("IT_TASK_BASKET")
        val baskets: List<CreateTaskBasketInfo> = emptyList(),
        /**  Таблица товаров разбитых по корзинам */
        @SerializedName("IT_TASK_BASKET_POS")
        val basketPositions: List<BasketPositionInfo> = emptyList()
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