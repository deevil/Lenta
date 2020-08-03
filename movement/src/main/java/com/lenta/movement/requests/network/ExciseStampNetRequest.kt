package com.lenta.movement.requests.network

import com.lenta.movement.exception.InfoFailure
import com.lenta.movement.models.ExciseStamp
import com.lenta.movement.requests.network.models.obtainingDataExciseGoodsNetRequest.ExciseGoodsParams
import com.lenta.movement.requests.network.models.obtainingDataExciseGoodsNetRequest.ExciseGoodsRestInfo
import com.lenta.movement.utils.DateTimeUtils
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class ExciseStampNetRequest @Inject constructor(
        private val obtainingDataExciseGoodsNetRequest: ObtainingDataExciseGoodsNetRequest
) : UseCase<ExciseStamp, ExciseStampParams> {

    override suspend fun run(params: ExciseStampParams): Either<Failure, ExciseStamp> {
        val baseResult = obtainingDataExciseGoodsNetRequest(
                params = ExciseGoodsParams(
                        werks = params.tk,
                        materialNumber = params.materialNumber,
                        materialNumberComp = "",
                        stampCode = params.stampCode,
                        boxNumber = "",
                        manufacturerCode = "",
                        bottlingDate = "",
                        mode = "1",
                        codeEBP = "MVM",
                        factCount = ""
                )
        )

        if (baseResult is Either.Left) return baseResult

        if (baseResult is Either.Right) {
            if (baseResult.b.status != ExciseGoodsRestInfo.InfoStatus.StampFound) {
                return Either.Left(InfoFailure(baseResult.b.statusTxt.orEmpty()))
            }

            val serverDate = baseResult.b.dateManufacture
            val dateOfPour = DateTimeUtils.formatServerDate(
                    serverDate = serverDate.orEmpty(),
                    serverDatePattern = DateTimeUtils.YYYY_MM_dd_DATE_PATTERN
            )

            val exciseStamp = ExciseStamp(
                    code = params.stampCode,
                    materialNumber = params.materialNumber,
                    manufacturerName = baseResult.b.manufacturers?.firstOrNull()?.name.orEmpty(),
                    dateOfPour = dateOfPour
            )

            return Either.Right(exciseStamp)
        }

        return Either.Left(Failure.NetworkConnection)
    }

}

data class ExciseStampParams(
        val tk: String,
        val materialNumber: String,
        val stampCode: String
)