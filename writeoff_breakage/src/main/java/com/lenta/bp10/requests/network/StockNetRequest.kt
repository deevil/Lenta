package com.lenta.bp10.requests.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.hhive.ANALYTICS_HELPER
import com.lenta.shared.utilities.extentions.hhive.getFailure
import com.lenta.shared.utilities.extentions.hhive.isNotBad
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import javax.inject.Inject

class StockNetRequest
@Inject constructor(private val hyperHive: HyperHive, private val sessionInfo: ISessionInfo, private val gson: Gson) : UseCase<StockLockRequestResult, Nothing?>() {
    override suspend fun run(params: Nothing?): Either<Failure, StockLockRequestResult> {
        val webCallParams = WebCallParams().apply {
            data = gson.toJson(mapOf("IV_PLANT" to sessionInfo.market))
            headers = mapOf(
                    "X-SUP-DOMAIN" to "DM-MAIN",
                    "Content-Type" to "application/json",
                    "Web-Authorization" to sessionInfo.basicAuth
            )
        }

        val resName = "ZMP_UTZ_02_V001"

        ANALYTICS_HELPER?.onStartFmpRequest(resName, "headers: ${webCallParams.headers}, data: ${webCallParams.data}")

        val status = hyperHive.requestAPI.web("ZMP_UTZ_02_V001", webCallParams, StockLockRequestStatus::class.java).execute()
        Logg.d { "StockNetRequest response: $status" }

        if (status.isNotBad() && status.result != null && status.result!!.raw != null) {
            return Either.Right(status.result!!.raw!!)
        }

        return Either.Left(status.getFailure())
    }
}


class StockLockRequestStatus : ObjectRawStatus<StockLockRequestResult>()

data class StockLockRequestResult(
        @SerializedName("ET_STORLOCS")
        val stocksLocks: List<StockLock>
)

data class StockLock(
        @SerializedName("STORLOC")
        val storloc: String,

        @SerializedName("LOCKED")
        val locked: String
)