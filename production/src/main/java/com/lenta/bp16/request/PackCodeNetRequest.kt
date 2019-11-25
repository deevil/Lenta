package com.lenta.bp16.request

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.request.pojo.GoodInfo
import com.lenta.bp16.request.pojo.RetCode
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class PackCodeNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<PackCodeResult, PackCodeParams> {

    override suspend fun run(params: PackCodeParams): Either<Failure, PackCodeResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_PRO_04_V001", params, PackCodeStatus::class.java)
                .rightToLeft(
                        fnRtoL = { result ->
                            result.retCodes.firstOrNull { retCode ->
                                retCode.retCode == 1
                            }?.let { retCode ->
                                return@rightToLeft Failure.SapError(retCode.errorText)
                            }
                        }
                )
    }

}

data class PackCodeParams(
        /** Код предприятия */
        @SerializedName("IV_WERKS")
        val marketNumber: String,
        /** Номер ЕО */
        @SerializedName("IV_EXIDV")
        val puNumber: String,
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val marketIp: String,
        /** Номер заказа */
        @SerializedName("AUFNR")
        val order: String,
        /** SAP – код товар */
        @SerializedName("IV_MATNR")
        val material: String,
        /** Фактическое количество сырья */
        @SerializedName("IV_FACT_QNT")
        val quantity: Double
)

class PackCodeStatus : ObjectRawStatus<PackCodeResult>()

data class PackCodeResult(
        /** Код тары */
        @SerializedName("EV_CODE_CONT")
        val packCode: List<GoodInfo>,
        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>
)