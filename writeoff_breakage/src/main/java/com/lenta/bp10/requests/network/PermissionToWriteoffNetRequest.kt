package com.lenta.bp10.requests.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.fmp.toFmpObjectRawStatusEither
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import javax.inject.Inject

class PermissionToWriteoffNetRequest
@Inject constructor(private val hyperHive: HyperHive, private val gson: Gson) : UseCase<PermissionToWriteoffRestInfo, PermissionToWriteoffPrams>(){
    override suspend fun run(params: PermissionToWriteoffPrams): Either<Failure, PermissionToWriteoffRestInfo> {
        val webCallParams = WebCallParams().apply {
            data = gson.toJson(params)
            headers = mapOf(
                    "X-SUP-DOMAIN" to "DM-MAIN",
                    "Content-Type" to "application/json"
            )
        }

        /**val resString = hyperHive.requestAPI.web("ZMP_UTZ_WOB_06_V001", webCallParams). execute()
        Logg.d { "resString: $resString" }*/
        val res = hyperHive.requestAPI.web("ZMP_UTZ_WOB_06_V001", webCallParams).execute().toFmpObjectRawStatusEither(PermissionToWriteoffStatus::class.java, gson)

        return res
    }

}

data class PermissionToWriteoffPrams(
        @SerializedName("IV_MATNR")
        val matnr: String,
        @SerializedName("IV_WERKS")
        val werks: String
)

class PermissionToWriteoffStatus : ObjectRawStatus<PermissionToWriteoffRestInfo>()

data class PermissionToWriteoffRestInfo(
        @SerializedName("EV_OWNPR")
        val ownr: String,
        @SerializedName("EV_RETCODE")
        val retcode: String,
        @SerializedName("EV_ERROR_TEXT")
        val err: String
)