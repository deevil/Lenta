package com.lenta.bp14.requests.tasks

import com.google.gson.annotations.SerializedName
import com.lenta.bp14.requests.pojo.RetCode
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
        @SerializedName("ET_TASK_LIST")
        val taskList: List<TaskInfo>,
        @SerializedName("ET_RETCODE")
        val retCodeList: List<RetCode>
)

data class TaskInfo(
        @SerializedName("TASK_NUM")
        val taskNumber: String,
        @SerializedName("TASK_TYPE")
        val taskType: String,
        @SerializedName("TEXT1")
        val text1: String,
        @SerializedName("TEXT2")
        val text2: String,
        @SerializedName("TEXT3")
        val text3: String,
        //Тип блокировки: 1 - своя, 2 - не своя
        @SerializedName("BLOCK_TYPE")
        val blockType: String,
        @SerializedName("LOCK_USER")
        val lockUser: String,
        @SerializedName("NOT_FINISH")
        val notFinished: String,
        // Количество позиций в задании
        @SerializedName("QNT_POS")
        val quantityPositions: Int,
        @SerializedName("DESCR")
        val taskName: String,
        @SerializedName("IS_STRICT")
        val isStrict: String,
        @SerializedName("COMMENT")
        val comment: String
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

