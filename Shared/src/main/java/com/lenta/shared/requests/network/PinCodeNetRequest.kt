package com.lenta.shared.requests.network

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.fmp.toFmpObjectRawStatusEither
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.extentions.hhive.toEither
import com.lenta.shared.utilities.getBaseAuth
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import javax.inject.Inject

class PinCodeNetRequest
@Inject constructor(private val hyperHive: HyperHive,
                    private val gson: Gson,
                    private val appSettings: IAppSettings,
                    private val sessionInfo: ISessionInfo) : UseCase<PinCodeInfo, PinCodeRequestParams?>() {
    override suspend fun run(params: PinCodeRequestParams?): Either<Failure, PinCodeInfo> {
        val isAuthorized = hyperHive.authAPI.isAuthorized

        var basicAuth = sessionInfo.basicAuth

        if (!isAuthorized) {
            val login = params?.login ?: appSettings.techLogin
            val password = params?.password ?: appSettings.techPassword
            basicAuth = getBaseAuth(login, password)
            hyperHive.authAPI.unAuth().execute()
            val status = hyperHive.authAPI.auth(login,
                    password, true).execute()
            if (!status.isOk) {
                return status.toEither(null)
            }
        }

        val res = hyperHive.requestAPI.web("ZMP_UTZ_90_V001", WebCallParams().apply {
            headers = mapOf(
                    "Web-Authorization" to basicAuth
            )
        }).execute().toFmpObjectRawStatusEither(PinCodeStatus::class.java, gson)

        if (!isAuthorized) {
            hyperHive.authAPI.unAuth().execute()
        }

        return res
    }

}


data class PinCodeRequestParams(val login: String, val password: String)

class PinCodeStatus : ObjectRawStatus<PinCodeInfo>()

data class PinCodeInfo(
        @Expose
        @SerializedName("EV_ERROR_TEXT")
        val errorText: String? = null,
        @Expose
        @SerializedName("EV_PINCODE")
        val pinCode: String? = null,
        @Expose
        @SerializedName("EV_RETCODE")
        val retCode: Int

)