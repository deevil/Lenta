package com.lenta.bp10.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class PermissionToWriteoffNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<PermissionToWriteoffRestInfo, PermissionToWriteoffPrams>() {
    override suspend fun run(params: PermissionToWriteoffPrams): Either<Failure, PermissionToWriteoffRestInfo> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_WOB_06_V001", params, PermissionToWriteoffStatus::class.java)
    }

}

data class PermissionToWriteoffPrams(
        @SerializedName("IV_MATNR")
        val matnr: String,
        @SerializedName("IV_WERKS")
        val werks: String
)

class PermissionToWriteoffStatus : ObjectRawStatus<PermissionToWriteoffRestInfo>()

data class PermissionToWriteoffRestInfo(
        @SerializedName("EV_OWNPR")
        val ownr: String,
        @SerializedName("EV_RETCODE")
        val retcode: String,
        @SerializedName("EV_ERROR_TEXT")
        val err: String
)
