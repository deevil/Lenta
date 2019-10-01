package com.lenta.bp10.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class ExciseStampNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<ExciseStampRestInfo, ExciseStampParams> {
    override suspend fun run(params: ExciseStampParams): Either<Failure, ExciseStampRestInfo> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_WOB_03_V001", params, ExciseStampStatus::class.java)
    }

}

data class ExciseStampParams(
        @SerializedName("IV_PDF417")
        val pdf417: String,
        @SerializedName("IV_WERKS")
        val werks: String,
        @SerializedName("IV_MATNR")
        val matnr: String)

class ExciseStampStatus : ObjectRawStatus<ExciseStampRestInfo>()

data class ExciseStampRestInfo(
        @SerializedName("EV_ERROR_TEXT")
        val errorText: String,
        @SerializedName("EV_RETCODE")
        val retCode: Int,
        @SerializedName("EV_MATNR")
        val matNr: String
)

