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

class MovementNewRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<MovementResult, MovementParams> {

    override suspend fun run(params: MovementParams): Either<Failure, MovementResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_PRO_13_V001", params, MovementStatus::class.java)
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

data class MovementParams(
        /**Номер ТК*/
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        /**Товар*/
        @SerializedName("IV_MATNR")
        val matnr: String,
        /**Код производителя*/
        @SerializedName("IV_PROD_CODE")
        val prodCode: String,
        /**Дата производства*/
        @SerializedName("IV_DATE_PROD")
        val dateProd: String,
        /**Срок годности*/
        @SerializedName("IV_EXPIR_DATE")
        val expirDate: String,
        /**Склад отправитель*/
        @SerializedName("IV_LGORT_EXPORT")
        val lgortExport: String,
        /**Склад получатель*/
        @SerializedName("IV_LGORT_IMPORT")
        val lgortImport: String,
        /**Код тары*/
        @SerializedName("IV_CODE_CONT")
        val codeCont: String,
        /**Количество*/
        @SerializedName("IV_FACT_QNT")
        val factQnt: String,
        /**Единицы измерения*/
        @SerializedName("IV_BUOM")
        val buom: String,
        /**IP устройства*/
        @SerializedName("IV_IP_PDA")
        val deviceIP: String,
        /**Табельный номер*/
        @SerializedName("IV_PERNR")
        val personnelNumber: String
)

class MovementStatus : ObjectRawStatus<MovementResult>()

data class MovementResult(
        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>
)