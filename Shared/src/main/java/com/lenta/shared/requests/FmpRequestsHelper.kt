package com.lenta.shared.requests

import com.google.gson.Gson
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.utilities.extentions.hhive.getFailure
import com.lenta.shared.utilities.extentions.hhive.isNotBad
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams

class FmpRequestsHelper(val hyperHive: HyperHive,
                        val defaultHeaders: Map<String, String>,
                        val sessionInfo: ISessionInfo, val gson: Gson,
                        val analyticsHelper: AnalyticsHelper) {

    fun <T : ObjectRawStatus<S>, S> restRequest(resourceName: String, data: Any?, clazz: Class<T>): Either<Failure, S> {

        val webCallParams = WebCallParams().apply {
            if (data != null) {
                this.data = if (data is String) data else gson.toJson(data)
            }
            headers = defaultHeaders.toMutableMap().also {
                it["Web-Authorization"] = sessionInfo.basicAuth ?: ""
            }
        }

        analyticsHelper.onStartFmpRequest(resourceName = resourceName)

        val status = hyperHive.requestAPI.web(resourceName, webCallParams, clazz)
                .execute()

        analyticsHelper.onFinishFmpRequest(resourceName = resourceName)

        if (status.isNotBad()) {
            val resultData = status.result?.raw
            return if (resultData is SapResponse && resultData.retCode != 0) {
                val errorText = resultData.errorText ?: ""
                analyticsHelper.onRetCodeNotEmpty("errorText: ${resultData.errorText}, retCode: ${resultData.retCode}")
                Either.Left(Failure.SapError(errorText))
            } else {
                Either.Right(status.result!!.raw!!)
            }
        }
        return Either.Left(status.getFailure())
    }

}

interface SapResponse {
    val errorText: String?
    val retCode: Int
}