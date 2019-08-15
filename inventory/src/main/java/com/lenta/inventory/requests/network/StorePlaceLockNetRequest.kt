package com.lenta.inventory.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import com.lenta.shared.utilities.Logg
import javax.inject.Inject

class StorePlaceLockNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<StorePlaceLockRestInfo, StorePlaceLockParams>() {
    override suspend fun run(params: StorePlaceLockParams): Either<Failure, StorePlaceLockRestInfo> {
        Logg.d { "IP: ${params.ip}\nTask number: ${params.taskNumber}\nPlace code: ${params.storePlaceCode}\nMode: ${params.mode}\n Personal number: ${params.userNumber}" }
        return fmpRequestsHelper.restRequest("ZMP_UTZ_97_V001", params, StorePlaceLockStatus::class.java)
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
        override val errorText: String, //Текст ошибки
        @SerializedName("EV_RETCODE")
        override val retCode: Int //Код возврата для ABAP-операторов
) : SapResponse

class StorePlaceLockStatus : ObjectRawStatus<StorePlaceLockRestInfo>()