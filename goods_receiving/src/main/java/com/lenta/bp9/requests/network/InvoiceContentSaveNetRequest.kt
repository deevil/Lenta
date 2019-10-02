package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.revise.CommentToVP
import com.lenta.bp9.model.task.revise.InvoiceContentEntryRestData
import com.lenta.bp9.model.task.revise.InvoiceReviseRestData
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class InvoiceContentSaveNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<InvoiceContentSaveRequestResult, InvoiceContentSaveRequestParameters> {
    override suspend fun run(params: InvoiceContentSaveRequestParameters): Either<Failure, InvoiceContentSaveRequestResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_18_V001", params, InvoiceContentSaveRequestStatus::class.java)
    }
}

class InvoiceContentSaveRequestStatus : ObjectRawStatus<InvoiceContentSaveRequestResult>()


data class InvoiceContentSaveRequestResult(
        @SerializedName("ES_TTN_CHK")
        val invoiceData: InvoiceReviseRestData,
        @SerializedName("ET_VBELN_COM")
        val notes: List<CommentToVP>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse

data class InvoiceContentSaveRequestParameters(
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_TTN_NUM")
        val invoiceNumber: String,
        @SerializedName("IV_TTN_DATE")
        val invoiceDate: String,
        @SerializedName("IT_VBELN_POS")
        val invoiceContents: List<InvoiceContentEntryRestData>,
        @SerializedName("IT_VBELN_COM")
        val notes: List<CommentToVP>
)