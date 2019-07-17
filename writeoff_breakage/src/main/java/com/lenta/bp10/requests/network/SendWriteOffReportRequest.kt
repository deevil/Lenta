package com.lenta.bp10.requests.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lenta.bp10.fmp.resources.send_report.WriteOffReport
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.fmp.toFmpObjectRawStatusEither
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.extentions.hhive.ANALYTICS_HELPER
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import javax.inject.Inject

class SendWriteOffReportRequest
@Inject constructor(private val hyperHive: HyperHive, private val gson: Gson, private val sessionInfo: ISessionInfo) : UseCase<WriteOffReportResponse, WriteOffReport>() {
    override suspend fun run(params: WriteOffReport): Either<Failure, WriteOffReportResponse> {
        val resName = "ZMP_UTZ_WOB_04_V001"
        return hyperHive.requestAPI.web(resName,
                WebCallParams().apply {
                    data = gson.toJson(params)
                    headers = mapOf(
                            "X-SUP-DOMAIN" to "DM-MAIN",
                            "Content-Type" to "application/json",
                            "Web-Authorization" to sessionInfo.basicAuth
                    )
                }.apply {
                    ANALYTICS_HELPER?.onStartFmpRequest(resName, "headers: ${this.headers}, data: ${this.data}")
                })
                .execute().toFmpObjectRawStatusEither(WriteOffReportStatus::class.java, gson)
    }
}

class WriteOffReportStatus : ObjectRawStatus<WriteOffReportResponse>()

data class WriteOffReportResponse(
        @SerializedName("ET_TASK_LIST")
        val taskList: List<TaskInfo>,
        @SerializedName("EV_ERROR_TEXT")
        val errorText: String,
        @SerializedName("EV_RETCODE")
        val retCode: String

)

data class TaskInfo(
        @SerializedName("TASK_NUM")
        val number: String,
        @SerializedName("TASK_TYPE")
        val taskType: String,
        @SerializedName("DESCR")
        val description: String

)