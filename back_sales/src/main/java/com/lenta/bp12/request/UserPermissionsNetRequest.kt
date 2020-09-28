package com.lenta.bp12.request

import com.google.gson.annotations.SerializedName
import com.lenta.bp12.request.pojo.MarketInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class UserPermissionsNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<PermissionsRequestResult, PermissionsRequestParams> {

    override suspend fun run(params: PermissionsRequestParams): Either<Failure, PermissionsRequestResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_BKS_01_V001", params, PermissionsRequestStatus::class.java)
    }

}

data class PermissionsRequestParams(
        @SerializedName("IV_UNAME")
        val userName: String
)

class PermissionsRequestStatus : ObjectRawStatus<PermissionsRequestResult>()

data class PermissionsRequestResult(
        /** Список адресов ТК */
        @SerializedName("ET_WERKS")
        val marketInfos: List<MarketInfo>?,
        /** Код возврата */
        @SerializedName("EV_RETCODE")
        override val retCode: Int?,
        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String?
) : SapResponse