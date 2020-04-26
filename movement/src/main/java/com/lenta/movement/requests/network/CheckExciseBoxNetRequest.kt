package com.lenta.movement.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class CheckExciseBoxNetRequest @Inject constructor(
    private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<CheckExciseBoxStatus, CheckExciseBoxParams> {

    override suspend fun run(params: CheckExciseBoxParams): Either<Failure, CheckExciseBoxStatus> {
        return fmpRequestsHelper.restRequest(
            resourceName = "ZMP_UTZ_MVM_05_V001",
            data = params,
            clazz = CheckExciseBoxRestStatus::class.java
        ).let {
            if (it is Either.Left) return@let it

            return@let Either.Right((it as Either.Right).b.toStatus())
        }
    }

}

sealed class CheckExciseBoxStatus {
    object Correct : CheckExciseBoxStatus()

    class BoxFound(val msg: String) : CheckExciseBoxStatus()

    class Other(val msg: String) : CheckExciseBoxStatus()
}

private fun CheckExciseBoxRestInfo.toStatus(): CheckExciseBoxStatus {
    return when (statusCode) {
        "01" -> CheckExciseBoxStatus.Correct
        "02" -> CheckExciseBoxStatus.BoxFound(statText)
        else -> CheckExciseBoxStatus.Other(statText)
    }
}

class CheckExciseBoxRestStatus : ObjectRawStatus<CheckExciseBoxRestInfo>()

data class CheckExciseBoxParams(
    @SerializedName("IV_WERKS")
    val tk: String,
    @SerializedName("IV_MATNR")
    val materialNumber: String,
    @SerializedName("IV_BOX_NUM")
    val boxCode: String
)

data class CheckExciseBoxRestInfo(
    @SerializedName("EV_STAT")
    val statusCode: String,
    @SerializedName("EV_STAT_TEXT")
    val statText: String,
    @SerializedName("EV_ERROR_TEXT")
    val errorText: String
)


