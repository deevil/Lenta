package com.lenta.bp16.request

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.request.pojo.DataLabel
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
        /** Тип родительской связи для создания тары: 1 - ЕО, 2 - ВП */
        @SerializedName("IV_MODE")
        val taskType: Int,
        /** Номер родительской связи */
        @SerializedName("IV_PARENT")
        val parent: String,
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
        val quantity: Double,
        /** Тип брака (категория) */
        @SerializedName("IV_TYPE_DEF")
        val categoryCode: String = "",
        /** Код причины брака */
        @SerializedName("IV_GRUND")
        val defectCode: String = "",
        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val personnelNumber: String
)

class PackCodeStatus : ObjectRawStatus<PackCodeResult>()

data class PackCodeResult(
        /** Структура данных для этикетки */
        @SerializedName("ES_DATA_LABEL")
        val dataLabel: DataLabel,
        /** Код тары */
        @SerializedName("EV_CODE_CONT")
        val packCode: String,
        /** ??? */
        @SerializedName("IS_AUTOFIX")
        val isAutofix: String,
        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>
)