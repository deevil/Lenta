package com.lenta.bp9.requests

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lenta.bp9.models.task.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.hhive.getFailure
import com.lenta.shared.utilities.extentions.hhive.isNotBad
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import javax.inject.Inject

class TaskListNetRequest
@Inject constructor(
        private val hyperHive: HyperHive,
        private val gson: Gson,
        private val sessionInfo: ISessionInfo,
        private val analyticsHelper: AnalyticsHelper
) : UseCase<TaskList, TaskListParams>() {

    override suspend fun run(params: TaskListParams): Either<Failure, TaskList> {
        val webCallParams = WebCallParams().apply {
            data = gson.toJson(params)
            headers = mapOf(
                    "X-SUP-DOMAIN" to "DM-MAIN",
                    "Content-Type" to "application/json",
                    "Web-Authorization" to sessionInfo.basicAuth
            )
        }

        val resName = "ZMP_UTZ_GRZ_02_V001"

        analyticsHelper.onStartFmpRequest(resourceName = resName, params = webCallParams.data)
        val status = hyperHive.requestAPI.web(resName, webCallParams, TaskContentStatus::class.java).execute()
        analyticsHelper.onFinishFmpRequest(resourceName = resName)

        Logg.d { "status: $status" }

        if (status.isNotBad()) {
            status.result?.raw?.let {
                if (it.retcode == "0") {
                    return Either.Right(it.taskList())
                } else {
                    return Either.Left(Failure.SapError(it.error))
                }
            }
        }

        return Either.Left(status.getFailure())
    }

    fun TaskRestInfo.taskInfo(): TaskInfo {
        return TaskInfo(position = position,
                taskNumber = taskNumber,
                topText = topText,
                bottomText = bottomText,
                caption = caption,
                taskType = TaskType.from(taskType),
                positionsCount = positionsCount.toInt(),
                status = TaskStatus.from(status),
                isStarted = isStarted.isNotEmpty(),
                lockStatus = TaskLockStatus.from(lockStatus),
                isDelayed = isDelayed.isNotEmpty(),
                isPaused = isPaused.isNotEmpty(),
                isCracked = isCracked.isNotEmpty(),
                documentNumber = documentNumber,
                transportationOTM = transportationOTM
        )
    }

    fun TaskListRestInfo.taskList(): TaskList {
        val tasks = taskList.map { it.taskInfo() }
        return TaskList(tasks = tasks,
                taskCount = taskCount.toInt()
        )
    }
}

data class TaskListParams(
        @SerializedName("IV_TYPE")
        val type: String,
        @SerializedName("IV_WERKS")
        val storeNumber: String,
        @SerializedName("IV_IP")
        val ip: String,
        @SerializedName("IV_PERNR")
        val userNumber: String,
        @SerializedName("IS_SEARCH_TASK")
        val searchParams: TaskListSearchParams?
)

data class TaskListSearchParams(
        @SerializedName("TASK_NUM")
        val taskNumber: String?,
        @SerializedName("LIFNR")
        val supplierNumber: String?,
        @SerializedName("EBELN")
        val documentNumber: String?,
        @SerializedName("ZTTN")
        val invoiceNumber: String?,
        @SerializedName("TRNUM")
        val transportNumber: String?,
        @SerializedName("EXIDV_TOP")
        val numberGE: String?,
        @SerializedName("EXIDV")
        val numberEO: String?
)

class TaskContentStatus : ObjectRawStatus<TaskListRestInfo>()

data class TaskListRestInfo(
        @SerializedName("ET_TASK_LIST")
        val taskList: List<TaskRestInfo>,
        @SerializedName("EV_NUM_TASK")
        val taskCount: String,
        @SerializedName("EV_ERROR_TEXT")
        val error: String, //Текст ошибки
        @SerializedName("EV_RETCODE")
        val retcode: String //Код возврата для ABAP-операторов
)

data class TaskRestInfo(
        @SerializedName("NUM_POS")
        val position: String,
        @SerializedName("TASK_NUM")
        val taskNumber: String,
        @SerializedName("TEXT1")
        val topText: String,
        @SerializedName("TEXT2")
        val bottomText: String,
        @SerializedName("TEXT3")
        val caption: String,
        @SerializedName("TASK_TYPE")
        val taskType: String,
        @SerializedName("QNT_POS")
        val positionsCount: String,
        @SerializedName("CUR_STAT")
        val status: String,
        @SerializedName("IS_STARTED")
        val isStarted: String,
        @SerializedName("IS_LOCK")
        val lockStatus: String,
        @SerializedName("IS_DELAY")
        val isDelayed: String,
        @SerializedName("IS_PAUSE")
        val isPaused: String,
        @SerializedName("IS_CRACK")
        val isCracked: String,
        @SerializedName("EBELN")
        val documentNumber: String,
        @SerializedName("TRNUM")
        val transportationOTM: String
)