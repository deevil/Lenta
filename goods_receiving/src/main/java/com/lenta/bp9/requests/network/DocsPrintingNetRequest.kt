package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.TaskDocumentsPrintingRestInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class DocsPrintingNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<DocsPrintingRestInfo, DocsPrintingParams> {
    override suspend fun run(params: DocsPrintingParams): Either<Failure, DocsPrintingRestInfo> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_03_V001", params, DocsPrintingStatus::class.java)
    }
}

data class DocsPrintingParams(
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String //Номер задания
)

class DocsPrintingStatus : ObjectRawStatus<DocsPrintingRestInfo>()

data class DocsPrintingRestInfo(
        @SerializedName("ET_DOC_PRINT") //Список документов для печати
        val listDocumentsPrinting: List<TaskDocumentsPrintingRestInfo>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse