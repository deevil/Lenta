package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.TaskNotificationRestInfo
import com.lenta.bp9.model.task.revise.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class StartReviseNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<StartReviseRequestResult, StartReviseRequestParameters>() {
    override suspend fun run(params: StartReviseRequestParameters): Either<Failure, StartReviseRequestResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_07_V001", params, StartReviseRequestStatus::class.java)
    }
}

class StartReviseRequestStatus : ObjectRawStatus<StartReviseRequestResult>()


data class StartReviseRequestResult(
        @SerializedName("ET_DOC_CHK")
        val deliveryReviseDocuments: List<DeliveryDocumentReviseRestData>,
        @SerializedName("ET_DOC_MATNR_CHK")
        val productReviseDocuments: List<DeliveryProductDocumentReviseRestData>,
        @SerializedName("ET_PARTS_CHK")
        val productBatches: List<ProductBatchReviseRestData>,
        @SerializedName("ET_FORMA_RUS")
        val russianABForms: List<FormABRussianReviseRestData>,
        @SerializedName("ET_FORMA_IMP")
        val importABForms: List<FormABImportReviseRestData>,
        @SerializedName("ET_COMP_CHK")
        val setComponents: List<SetComponentReviseRestData>,
        @SerializedName("ES_TTN_CHK")
        val invoiceData: InvoiceReviseRestData,

        @SerializedName("ET_DOC_NOTIFY")
        val documentNotifications: List<TaskNotificationRestInfo>,
        @SerializedName("ET_MATNR_NOTIFY")
        val productNotifications: List<TaskNotificationRestInfo>,

        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse

data class StartReviseRequestParameters(
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_DATE_VERIFY")
        val reviseStartDate: String,
        @SerializedName("IV_TIME_VERIFY")
        val reviseStartTime: String,
        @SerializedName("IV_DATE_ARRIV")
        val arrivalDate: String,
        @SerializedName("IV_TIME_ARRIV")
        val arrivalTime: String
)