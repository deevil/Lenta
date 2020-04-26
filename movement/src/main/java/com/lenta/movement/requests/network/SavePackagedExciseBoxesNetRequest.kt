package com.lenta.movement.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.movement.exception.InfoFailure
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.BaseRestSapStatus
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class SavePackagedExciseBoxesNetRequest @Inject constructor(
    private val fmpRequestsHelper: FmpRequestsHelper
): UseCase<Unit, SavePackagedExciseBoxesParams> {

    override suspend fun run(params: SavePackagedExciseBoxesParams): Either<Failure, Unit> {
        val status = fmpRequestsHelper.restRequest(
            resourceName = "ZMP_UTZ_MVM_07_V001",
            data = params,
            clazz = SavePackagedExciseBoxesStatus::class.java
        )

        if (status is Either.Left) return status

        if (status is Either.Right && status.b.retCode == "1") {
            return Either.Left(InfoFailure(status.b.errorText))
        }

        return Either.Right(Unit)
    }

}

data class SavePackagedExciseBoxesParams(
    @SerializedName("IV_PERNR")
    val userNumber: String,
    @SerializedName("IV_IP_PDA")
    val deviceIp: String,
    @SerializedName("IT_BOX_LIST")
    val boxes: List<Box>,
    @SerializedName("IT_MARK_LIST")
    val stamps: List<Stamp>
) {
    data class Box(
        @SerializedName("MATNR")
        val materialNumber: String,
        @SerializedName("BOX_NUM")
        val code: String
    )

    data class Stamp(
        @SerializedName("BOX_NUM")
        val boxCode: String,
        @SerializedName("MARK_NUM")
        val code: String
    )
}

class SavePackagedExciseBoxesStatus : ObjectRawStatus<BaseRestSapStatus>()