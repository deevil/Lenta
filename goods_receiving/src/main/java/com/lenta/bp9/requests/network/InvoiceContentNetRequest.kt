package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.revise.CommentToVP
import com.lenta.bp9.model.task.revise.InvoiceContentEntryRestData
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class InvoiceContentNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<InvoiceContentRequestResult, InvoiceContentRequestParameters> {
    override suspend fun run(params: InvoiceContentRequestParameters): Either<Failure, InvoiceContentRequestResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_17_V001", params, InvoiceContentRequestStatus::class.java)
    }
}

class InvoiceContentRequestStatus : ObjectRawStatus<InvoiceContentRequestResult>()


data class InvoiceContentRequestResult(
        @SerializedName("ET_VBELN_POS")
        val invoiceContents: List<InvoiceContentEntryRestData>,
        @SerializedName("ET_VBELN_COM")
        val notes: List<CommentToVP>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse

data class InvoiceContentRequestParameters(
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String
)