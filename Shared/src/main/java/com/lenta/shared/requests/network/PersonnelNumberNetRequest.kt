package com.lenta.shared.requests.network

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.extentions.hhive.getFailure
import com.lenta.shared.utilities.extentions.hhive.isNotBad
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import javax.inject.Inject

class PersonnelNumberNetRequest
@Inject constructor(private val hyperHive: HyperHive, private val gson: Gson, private val sessionInfo: ISessionInfo) : UseCase<TabNumberInfo, TabNumberParams>() {
    override suspend fun run(params: TabNumberParams): Either<Failure, TabNumberInfo> {
        val webCallParams = WebCallParams().apply {
            data = "{\"IV_PERNR\":\"${params.tabNumber}\"}"
            headers = mapOf(
                    "X-SUP-DOMAIN" to "DM-MAIN",
                    "Content-Type" to "application/json",
                    "Web-Authorization" to sessionInfo.basicAuth
            )
        }

        val status = hyperHive.requestAPI.web("ZMP_UTZ_98_V001", webCallParams, TabNumberStatus::class.java)
                .execute()

        if (status.isNotBad()) {
            val errorText = status.result?.raw?.errorText
            return if (errorText.isNullOrEmpty()) {
                Either.Right(status.result!!.raw!!)
            } else {
                Either.Left(Failure.SapError(errorText))
            }

        }
        return Either.Left(status.getFailure())
    }
}


data class TabNumberParams(val tabNumber: String)

class TabNumberStatus : ObjectRawStatus<TabNumberInfo>()

data class TabNumberInfo(
        @Expose
        @SerializedName("EV_ERROR_TEXT")
        val errorText: String? = null,
        @Expose
        @SerializedName("EV_JOBNAME")
        val jobName: String? = null,
        @Expose
        @SerializedName("EV_NAME")
        val name: String? = null,
        @Expose
        @SerializedName("EV_RETCODE")
        val retCode: Int

)