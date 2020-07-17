package com.lenta.bp16.request

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.request.pojo.TaskInfo
import com.lenta.bp16.request.pojo.RetCode
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class TaskListNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<TaskListResult, TaskListParams> {

    override suspend fun run(params: TaskListParams): Either<Failure, TaskListResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_PRO_02_V001", params, TaskListStatus::class.java)
                .rightToLeft(
                        fnRtoL = { result ->
                            result.retCodes.firstOrNull { retCode ->
                                retCode.retCode == 1
                            }?.let { retCode ->
                                return@rightToLeft Failure.SapError(retCode.errorText)
                            }
                        }
                )
    }
}

data class TaskListParams(
        /** Номер ТК */
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        /** Режим обработки: 1 - получение списка ЕО, 2 - получение списка ВП */
        @SerializedName("IV_MODE")
        val taskType: Int,
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val deviceIp: String
)

class TaskListStatus : ObjectRawStatus<TaskListResult>()

data class TaskListResult(
        /** Список объектов */
        @SerializedName("ET_OBJ_LIST")
        val tasks: List<TaskInfo>,
        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>
)