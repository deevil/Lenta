package com.lenta.bp10.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class ExciseStampNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<List<ExciseStampRestInfo>, ExciseStampParams>() {
    override suspend fun run(params: ExciseStampParams): Either<Failure, List<ExciseStampRestInfo>> {
        return fmpRequestsHelper.restRequest("ZFMP_UTZ_WOB_03_V001", params, ExciseStampStatus::class.java)
    }

}

data class ExciseStampParams(
        @SerializedName("IV_PDF417")
        val pdf417: String,
        @SerializedName("IV_WERKS")
        val werks: String,
        @SerializedName("IV_MATNR")
        val matnr: String)

class ExciseStampStatus : ObjectRawStatus<List<ExciseStampRestInfo>>()

data class ExciseStampRestInfo(
        @SerializedName("name")
        val name: String,
        @SerializedName("data")
        val data: List<List<String>>
)
