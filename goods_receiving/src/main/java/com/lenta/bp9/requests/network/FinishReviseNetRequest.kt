package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.TaskDescriptionRestInfo
import com.lenta.bp9.model.task.TaskMercuryNotActualRestData
import com.lenta.bp9.model.task.TaskNotificationRestInfo
import com.lenta.bp9.model.task.revise.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class FinishReviseNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<FinishReviseRequestResult, FinishReviseRequestParameters> {
    override suspend fun run(params: FinishReviseRequestParameters): Either<Failure, FinishReviseRequestResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_08_V001", params, FinishReviseRequestStatus::class.java)
    }
}

data class FinishReviseRequestParameters(
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IT_DOC_CHK")
        val deliveryReviseDocuments: List<DeliveryDocumentReviseRestData>,
        @SerializedName("IT_DOC_MATNR_CHK")
        val productReviseDocuments: List<DeliveryProductDocumentReviseRestData>,
        @SerializedName("IT_PARTS_CHK")
        val productBatches: List<ProductBatchReviseRestData>,
        @SerializedName("IT_FORMA_RUS")
        val russianABForms: List<FormABRussianReviseRestData>,
        @SerializedName("IT_FORMA_IMP")
        val importABForms: List<FormABImportReviseRestData>,
        @SerializedName("IS_TTN_CHK")
        val invoiceData: InvoiceReviseRestData?,
        @SerializedName("IT_VBELN_COM")
        val commentsToVP: List<CommentToVPRestData>,
        @SerializedName("IT_VET_CHK")
        val productVetDocument: List<ProductVetDocumentReviseRestData>,
        @SerializedName("IT_CMPLX_DOC")
        val complexDocument: List<ComplexDocumentReviseRestData>
)

class FinishReviseRequestStatus : ObjectRawStatus<FinishReviseRequestResult>()


data class FinishReviseRequestResult(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo,
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>,
        @SerializedName("ET_VET_NOT_ACTUAL")
        val taskMercuryNotActualRestData: List<TaskMercuryNotActualRestData>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse
