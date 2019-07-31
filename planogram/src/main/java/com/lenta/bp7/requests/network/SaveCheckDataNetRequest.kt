package com.lenta.bp7.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject


class SaveSelfControlDataNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<SaveCheckDataRestInfo, SaveCheckDataParams>() {
    override suspend fun run(params: SaveCheckDataParams): Either<Failure, SaveCheckDataRestInfo> {
        return fmpRequestsHelper.restRequest("SQL_Q_01Mp", params, SaveCheckDataStatus::class.java)
    }
}

class SaveExternalAuditDataNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<SaveCheckDataRestInfo, SaveCheckDataParams>() {
    override suspend fun run(params: SaveCheckDataParams): Either<Failure, SaveCheckDataRestInfo> {
        return fmpRequestsHelper.restRequest("SQL_P_01Mp", params, SaveCheckDataStatus::class.java)
    }
}

data class SaveCheckDataParams(
        @SerializedName("shop")
        val shop: String,
        @SerializedName("terminalId")
        val terminalId: String,
        @SerializedName("data")
        val data: String,
        @SerializedName("saveDoc")
        val saveDoc: Int)

class SaveCheckDataStatus : ObjectRawStatus<SaveCheckDataRestInfo>()

data class SaveCheckDataRestInfo(
        @SerializedName("Return")
        val codeReturn: Int
)