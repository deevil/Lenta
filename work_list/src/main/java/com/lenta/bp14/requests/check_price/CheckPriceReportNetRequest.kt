package com.lenta.bp14.requests.check_price

import com.google.gson.annotations.SerializedName
import com.lenta.bp14.models.check_price.CheckPriceResult
import com.lenta.bp14.models.check_price.CheckPriceTaskDescription
import com.lenta.bp14.requests.pojo.Position
import com.lenta.bp14.requests.pojo.ReportSentStatus
import com.lenta.bp14.requests.pojo.SentReportResult
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.utilities.extentions.toSapBooleanString
import javax.inject.Inject

class CheckPriceReportNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<SentReportResult, CheckPriceReport> {

    override suspend fun run(params: CheckPriceReport): Either<Failure, SentReportResult> {

        return fmpRequestsHelper.restRequest("ZMP_UTZ_WKL_07_V001",
                FmpReport(
                        description = params.description.taskName,
                        ip = params.ip,
                        isNotFinished = params.isNotFinish.toSapBooleanString(),
                        taskNumber = params.description.taskNumber,
                        tkNumber = params.description.tkNumber,
                        checksResult = params.checksResults.filter { !it.isMissing }.map {
                            CheckResult(
                                    matNr = it.matNr!!,
                                    statCheck = when (it.isAllValid()) {
                                        true -> "1"
                                        false -> "2"
                                        else -> "3"
                                    },
                                    isPrinted = it.isPrinted.toSapBooleanString()
                            )
                        },
                        positions = mutableListOf<Position>().apply {
                            params.checksResults.forEach {
                                add(Position(
                                        matNr = it.matNr!!,
                                        isProcessed = true.toSapBooleanString(),
                                        quantity = 0.0
                                ))
                            }

                            params.notProcessedResults.forEach {
                                add(Position(
                                        matNr = it.matNr!!,
                                        isProcessed = false.toSapBooleanString(),
                                        quantity = 0.0
                                ))
                            }
                        }
                )
                , ReportSentStatus::class.java)
                .rightToLeft { sentResult ->
                    sentResult.retCodes.firstOrNull { it.retCode == 1 }?.let {
                        Failure.SapError(it.errorText)
                    }
                }
                .map {
                    SentReportResult(
                            it.createdTasks
                    )
                }
    }

}


data class CheckPriceReport(
        val ip: String,
        val description: CheckPriceTaskDescription,
        val isNotFinish: Boolean,
        val checksResults: List<CheckPriceResult>,
        val notProcessedResults: List<CheckPriceResult>
)

data class FmpReport(
        /** Название задания */
        @SerializedName("IV_DESCR")
        val description: String,
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val ip: String,
        /** Обработка задания не закончена */
        @SerializedName("IV_NOT_FINISH")
        val isNotFinished: String,
        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        /** Номер ТК */
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        /** Таблица сверки цен */
        @SerializedName("IT_CHECK_PRICE")
        val checksResult: List<CheckResult>,
        /** Таблица состава задания */
        @SerializedName("IT_TASK_POS")
        val positions: List<Position>
)

data class CheckResult(
        /** SAP-код товара */
        @SerializedName("MATNR")
        val matNr: String,
        /** Статус проверки: 1 - верно, 2 - ошибка, 3 - ценник отсутствует */
        @SerializedName("STAT_CHECK")
        val statCheck: String,
        /** Признак – печать выполнена */
        @SerializedName("IS_PRINT")
        val isPrinted: String
)