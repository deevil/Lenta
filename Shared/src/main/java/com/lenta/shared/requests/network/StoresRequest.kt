package com.lenta.shared.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.fmp.resources.dao_ext.getAllMarkets
import com.lenta.shared.fmp.resources.fast.ZmpUtz23V001
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject


class StoresRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper, hyperHive: HyperHive) : UseCase<StoresRequestResult, Nothing?>() {

    var zmpUtz23V001: ZmpUtz23V001 = ZmpUtz23V001(hyperHive)

    override suspend fun run(params: Nothing?): Either<Failure, StoresRequestResult> {
        return Either.Right(StoresRequestResult(
                markets = zmpUtz23V001.getAllMarkets().map {
                    Market(
                            number = it.werks,
                            address = it.addres,
                            retailType = it.retailType
                    )
                },
                errorText = "",
                retCode = 0
        ))
    }
}

class StoresRequestStatus : ObjectRawStatus<StoresRequestResult>()

data class StoresRequestResult(
        @SerializedName("ET_WERKS")
        val markets: List<Market>,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String?,
        @SerializedName("EV_RETCODE")
        override val retCode: Int
) : SapResponse

data class Market(
        @SerializedName("WERKS")
        val number: String,
        @SerializedName("ADDRES")
        val address: String,
        @SerializedName("RETAIL_TYPE")
        val retailType: String
)