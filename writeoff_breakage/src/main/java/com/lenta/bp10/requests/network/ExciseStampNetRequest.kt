package com.lenta.bp10.requests.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.fmp.toFmpObjectRawStatusEither
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import javax.inject.Inject

class ExciseStampNetRequest
@Inject constructor(private val hyperHive: HyperHive, private val gson: Gson, private val sessionInfo: ISessionInfo) : UseCase<List<ExciseStampRestInfo>, ExciseStampParams>(){
    override suspend fun run(params: ExciseStampParams): Either<Failure, List<ExciseStampRestInfo>> {

        Logg.d { "search stamp params: $params" }

        val webCallParams = WebCallParams().apply {
            data = gson.toJson(params)
            headers = mapOf(
                    "X-SUP-DOMAIN" to "DM-MAIN",
                    "Content-Type" to "application/json",
                    "Web-Authorization" to sessionInfo.basicAuth
            )
        }
        return hyperHive.requestAPI.web("ZFMP_UTZ_WOB_03_V001", webCallParams).execute().toFmpObjectRawStatusEither(ExciseStampStatus::class.java, gson)
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
