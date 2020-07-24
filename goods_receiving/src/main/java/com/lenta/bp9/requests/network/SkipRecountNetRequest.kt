package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.TaskDescriptionRestInfo
import com.lenta.bp9.model.task.TaskNotificationRestInfo
import com.lenta.bp9.model.task.TaskSectionProductsRestData
import com.lenta.bp9.model.task.TaskSectionRestData
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class SkipRecountNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<SkipRecountResult, SkipRecountParameters> {
    override suspend fun run(params: SkipRecountParameters): Either<Failure, SkipRecountResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_29_V001", params, SkipRecountStatus::class.java)
    }
}

data class SkipRecountParameters(
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IV_COMMENT")
        val comment: String
)

class SkipRecountStatus : ObjectRawStatus<SkipRecountResult>()

data class SkipRecountResult(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo,
        @SerializedName("ET_TASK_PERNR")
        val sectionsInfo: List<TaskSectionRestData>,
        @SerializedName("ET_MATNR_ABTNR")
        val sectionProducts: List<TaskSectionProductsRestData>,
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse