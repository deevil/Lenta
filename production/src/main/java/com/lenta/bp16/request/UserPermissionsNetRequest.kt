package com.lenta.bp16.request

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.request.pojo.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class UserPermissionsNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<PermissionsRequestResult, PermissionsRequestParams> {
    override suspend fun run(params: PermissionsRequestParams): Either<Failure, PermissionsRequestResult> {
        return fmpRequestsHelper.restRequest(
                "ZMP_UTZ_PRO_01_V001",
                params,
                PermissionsRequestStatus::class.java)
                .rightToLeft { sentResult ->
                    sentResult.retCodes.firstOrNull { it.retCode == 1 }?.let {
                        Failure.SapError(it.errorText)
                    }
                }
    }
}


data class PermissionsRequestParams(
        @SerializedName("IV_UNAME")
        val userName: String
)

data class PermissionsRequestResult(
        @SerializedName("ET_WERKS")
        val markets: List<Market>,
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>
)

class PermissionsRequestStatus : ObjectRawStatus<PermissionsRequestResult>()