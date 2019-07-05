package com.lenta.inventory.requests.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.fmp.toFmpObjectRawStatusEither
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import javax.inject.Inject

class TaskListNetRequest
@Inject constructor(private val hyperHive: HyperHive, private val gson: Gson, private val sessionInfo: ISessionInfo) : UseCase<TasksListRestInfo, TasksListParams>(){
    override suspend fun run(params: TasksListParams): Either<Failure, TasksListRestInfo> {

        val webCallParams = WebCallParams().apply {
            data = gson.toJson(params)
            headers = mapOf(
                    "X-SUP-DOMAIN" to "DM-MAIN",
                    "Content-Type" to "application/json",
                    "Web-Authorization" to sessionInfo.basicAuth
            )
        }

        /**val resString = hyperHive.requestAPI.web("ZMP_UTZ_91_V001", webCallParams). execute()
        Logg.d { "resString: $resString" }*/
        val res = hyperHive.requestAPI.web("ZMP_UTZ_91_V001", webCallParams).execute().toFmpObjectRawStatusEither(TasksListStatus::class.java, gson)

        return res
    }

}
data class TasksListParams(
        @SerializedName("IV_WERKS")
        val werks: String,
        @SerializedName("IV_EXEC_USER")
        val user: String)

class TasksListStatus : ObjectRawStatus<TasksListRestInfo>()

data class TasksListRestInfo(
        @SerializedName("ET_TASK_LIST")
        val tasks: List<TasksItem>,
        @SerializedName("EV_ERROR_TEXT")
        val error: String,
        @SerializedName("EV_RETCODE")
        val retcode: String
)

data class TasksItem(
        @SerializedName("TASK_NUM")
        val taskNumber: String,
        @SerializedName("TASK_NAME")
        val taskName: String,
        @SerializedName("TASK_TYPE")
        val taskType: String,
        @SerializedName("LGORT")
        val stock: String,
        @SerializedName("IS_RECOUNT")
        val isRecount: String,
        @SerializedName("IS_STRICT")
        val isStrict: String,
        @SerializedName("BLOCK_TYPE")
        val blockType: String,
        @SerializedName("LOCK_USER")
        val lockUser: String,
        @SerializedName("LOCK_IP")
        val lockIP: String,
        @SerializedName("NUM_POS")
        val productsInTask: String,
        @SerializedName("NOT_FINISH")
        val notFinish: String,
        @SerializedName("DATE_FROM")
        val dateFrom: String,
        @SerializedName("DATE_TO")
        val dateTo: String,
        @SerializedName("MODE")
        val mode: String,
        @SerializedName("GIS_CNTRL")
        val gis: String
)