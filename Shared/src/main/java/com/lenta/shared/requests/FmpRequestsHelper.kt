package com.lenta.shared.requests

import com.google.gson.Gson
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.hhive.isNotBad
import com.lenta.shared.utilities.extentions.hhive.toEither
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import com.mobrun.plugin.models.BaseStatus

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
                it["Web-Authorization"] = sessionInfo.basicAuth.orEmpty()
            }
        }

        analyticsHelper.onStartFmpRequest(resourceName = resourceName)

        Logg.d { "webCallParams.data: ${webCallParams.data}" }

        val statusString = hyperHive.requestAPI.web(resourceName, webCallParams)
                .execute()

        var result: Either<Failure, S>

        try {
            Logg.d { "status: $statusString" }
            val status = gson.fromJson(statusString, clazz)
            result = if (status.isNotBad()) {
                analyticsHelper.onFinishFmpRequest(resourceName = resourceName)
                val resultData = status.result?.raw
                if (resultData is SapResponse && resultData.retCode != null && resultData.retCode != 0) {
                    val errorText = resultData.errorText.orEmpty()
                    analyticsHelper.onRetCodeNotEmpty("errorText: ${resultData.errorText}, retCode: ${resultData.retCode}, sent data: ${webCallParams.data}")
                    Either.Left(Failure.SapError(message = errorText, retCode = resultData.retCode))
                } else {
                    Either.Right(status.result!!.raw!!)
                }
            } else {
                status.toEither(status.result?.raw, resourceName = resourceName)
            }

        } catch (e: Exception) {
            Logg.w { "e: $e" }
            val status = gson.fromJson(statusString, BaseStatus::class.java)
            Logg.d { "status: $status" }
            result = status.toEither(data = null, resourceName = resourceName)
        }

        return result
    }
}

interface SapResponse {
    val errorText: String?
    val retCode: Int?
}