package com.lenta.bp16.request

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.request.pojo.GoodInfo
import com.lenta.bp16.request.pojo.PackInfo
import com.lenta.bp16.request.pojo.RawInfo
import com.lenta.bp16.request.pojo.RetCode
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class TaskInfoNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<TaskInfoResult, TaskInfoParams> {

    override suspend fun run(params: TaskInfoParams): Either<Failure, TaskInfoResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_PRO_03_V001", params, TaskInfoStatus::class.java)
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

data class TaskInfoParams(
        /** Код предприятия */
        @SerializedName("IV_WERKS")
        val marketNumber: String,
        /** Номер ЕО */
        @SerializedName("IV_EXIDV")
        val puNumber: String,
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val marketIp: String,
        /** Режим обработки: 1 - блокировка ЕО, 2 - переблокировка ЕО */
        @SerializedName("IV_MODE")
        val processingMode: String

)

class TaskInfoStatus : ObjectRawStatus<TaskInfoResult>()

data class TaskInfoResult(
        /** Список товаров */
        @SerializedName("ET_EXIDV_POS")
        val goods: List<GoodInfo>,
        /** Список первых переделов */
        @SerializedName("ET_FIRST_PRO")
        val raws: List<RawInfo>,
        /** Список товаров тары */
        @SerializedName("ET_CONT_POS")
        val packs: List<PackInfo>,
        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>
)