package com.lenta.bp10.requests.network

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.hhive.toEither
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import javax.inject.Inject

class TabNumberNetRequest
@Inject constructor(private val hyperHive: HyperHive, private val gson: Gson) : UseCase<TabNumberInfo, TabNumberParams>() {
    override suspend fun run(params: TabNumberParams): Either<Failure, TabNumberInfo> {
        val webCallParams = WebCallParams()
        webCallParams.data = "IV_PERNR = ${params.tabNumber}"
        val status = hyperHive.requestAPI.web("ZMP_UTZ_98_V001", webCallParams, TabNumberStatus::class.java).execute()
        Logg.d { "status: $status" }
        return status.toEither(status.result?.raw)
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