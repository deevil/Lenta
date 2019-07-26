package com.lenta.bp10.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp10.fmp.resources.send_report.WriteOffReport
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class SendWriteOffReportRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<WriteOffReportResponse, WriteOffReport>() {
    override suspend fun run(params: WriteOffReport): Either<Failure, WriteOffReportResponse> {
        return fmpRequestsHelper.restRequest(
                resourceName = "ZMP_UTZ_WOB_04_V001",
                data = params,
                clazz = WriteOffReportStatus::class.java)
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