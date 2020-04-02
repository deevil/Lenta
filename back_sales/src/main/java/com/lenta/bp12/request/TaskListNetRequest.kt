package com.lenta.bp12.request

import com.google.gson.annotations.SerializedName
import com.lenta.bp12.request.pojo.TaskInfo
import com.lenta.bp12.request.pojo.TaskSearchParams
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class TaskListNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<TaskListResult, TaskListParams> {

    override suspend fun run(params: TaskListParams): Either<Failure, TaskListResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_BKS_02_V001", params, TaskListStatus::class.java)
    }

}

data class TaskListParams(
        /** Номер ТК */
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        /** Адресат */
        @SerializedName("IV_EXEC_USER")
        val userAddress: String,
        /** Режим работы: 1 - обновление списка заданий, 2 - расширенный поиск заданий */
        @SerializedName("IV_MODE")
        val mode: Int,
        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val userNumber: String,
        /** Структура для поиска заданий */
        @SerializedName("IS_SEARCH_TASK")
        val taskSearchParams: TaskSearchParams? = null
)

class TaskListStatus : ObjectRawStatus<TaskListResult>()

data class TaskListResult(
        /** Список заданий */
        @SerializedName("ET_TASK_LIST")
        val tasks: List<TaskInfo>,
        /** Код возврата */
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse