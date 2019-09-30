package com.lenta.bp14.requests.tasks

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class TaskListNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<TaskListInfo, TasksListParams>() {

    override suspend fun run(params: TasksListParams): Either<Failure, TaskListInfo> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_WKL_02_V001", params, TaskListInfoStatus::class.java)

    }
}


class TaskListInfoStatus : ObjectRawStatus<TaskListInfo>()


data class TaskListInfo(
        @SerializedName("TASK_NUM")
        val taskNumber: String

)


data class TasksListParams(
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        @SerializedName("IV_EXEC_USER")
        val user: String,
        //режим работы ФМ: 1 - обновление, 2 - расширенный поиск
        @SerializedName("IV_MODE")
        val mode: String,
        @SerializedName("IS_SEARCH_TASK")
        val filter: SearchTaskFilter?
)

data class SearchTaskFilter(
        @SerializedName("TASK_TYPE")
        val taskType: String,
        @SerializedName("MATNR")
        val matNr: String,
        @SerializedName("ABTNR")
        val sectionId: String,
        @SerializedName("MATKL")
        val group: String,
        @SerializedName("DATE_PUBLIC")
        val dateOfPublic: String
)

