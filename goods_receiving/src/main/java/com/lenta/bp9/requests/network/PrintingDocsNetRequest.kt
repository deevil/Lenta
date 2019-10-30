package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.TaskDocumentsPrinting
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class PrintingDocsNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<PrintingDocsRestInfo, PrintingDocsParams> {
        override suspend fun run(params: PrintingDocsParams): Either<Failure, PrintingDocsRestInfo> {
                return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_04_V001", params, PrintingDocsStatus::class.java)
        }
}

data class PrintingDocsParams(
        @SerializedName("IT_DOC_PRINT")
        val listDocumentsPrinting: List<TaskDocumentsPrinting>, //Список документов для печати
        @SerializedName("IV_PRINTERNAME")
        val printerName: String //Спул: устройство вывода
)

class PrintingDocsStatus : ObjectRawStatus<PrintingDocsRestInfo>()

data class PrintingDocsRestInfo(
        @SerializedName("EV_RETCODE")
        val retcode: String,
        @SerializedName("EV_ERROR_TEXT")
        val errorText: String
)