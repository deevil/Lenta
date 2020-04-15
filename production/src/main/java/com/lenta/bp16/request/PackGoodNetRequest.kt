package com.lenta.bp16.request

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.request.pojo.RetCode
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class PackGoodNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<PackGoodResult, PackGoodParams> {

    override suspend fun run(params: PackGoodParams): Either<Failure, PackGoodResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_PRO_07_V001", params, PackGoodStatus::class.java)
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

data class PackGoodParams(
        /** Код предприятия */
        @SerializedName("IV_WERKS")
        val marketNumber: String,
        /** Тип родительской связи для создания тары: 1 - ЕО, 2 - ВП */
        @SerializedName("IV_MODE")
        val taskType: Int,
        /** Номер объекта */
        @SerializedName("IV_PARENT")
        val taskNumber: String,
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val deviceIp: String,
        /** Номер технологического заказа */
        @SerializedName("IV_AUFNR")
        val order: String,
        /** SAP – код товар */
        @SerializedName("IV_MATNR")
        val material: String,
        /** Фактическое количество сырья */
        @SerializedName("IV_FACT_QNT")
        val quantity: Double
)

class PackGoodStatus : ObjectRawStatus<PackGoodResult>()

data class PackGoodResult(
        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>
)