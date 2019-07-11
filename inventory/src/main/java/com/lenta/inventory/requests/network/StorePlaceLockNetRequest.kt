package com.lenta.inventory.requests.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.fmp.toFmpObjectRawStatusEither
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import javax.inject.Inject

class StorePlaceLockNetRequest
@Inject constructor(private val hyperHive: HyperHive, private val gson: Gson, private val sessionInfo: ISessionInfo) : UseCase<StorePlaceLockRestInfo, StorePlaceLockParams>(){
    override suspend fun run(params: StorePlaceLockParams): Either<Failure, StorePlaceLockRestInfo> {

        val webCallParams = WebCallParams().apply {
            data = gson.toJson(params)
            headers = mapOf(
                    "X-SUP-DOMAIN" to "DM-MAIN",
                    "Content-Type" to "application/json",
                    "Web-Authorization" to sessionInfo.basicAuth
            )
        }

        val res = hyperHive.requestAPI.web("ZMP_UTZ_97_V001", webCallParams).execute().toFmpObjectRawStatusEither(StorePlaceLockStatus::class.java, gson)

        return res
    }
}

data class StorePlaceLockParams(
        @SerializedName("IV_IP_PDA")
        val ip: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_PLACE_CODE")
        val storePlaceCode: String,
        @SerializedName("IV_MODE")
        val mode: String, //Режим работы ( 1 - блокировка, 2 - разблокировка, 3 - проверка чужих блокирово)
        @SerializedName("IV_PERNR")
        val userNumber: String
)

data class StorePlaceLockRestInfo(
        @SerializedName("EV_ERROR_TEXT")
        val error: String, //Текст ошибки
        @SerializedName("EV_RETCODE")
        val retcode: String //Код возврата для ABAP-операторов
)

class StorePlaceLockStatus : ObjectRawStatus<StorePlaceLockRestInfo>()