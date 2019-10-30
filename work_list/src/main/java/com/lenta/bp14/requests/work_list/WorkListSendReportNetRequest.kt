package com.lenta.bp14.requests.work_list

import com.google.gson.annotations.SerializedName
import com.lenta.bp14.models.work_list.Good
import com.lenta.bp14.models.work_list.WorkListTaskDescription
import com.lenta.bp14.requests.pojo.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.utilities.extentions.getFormattedDate
import com.lenta.shared.utilities.extentions.toSapBooleanString
import javax.inject.Inject

class WorkListSendReportNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<SentReportResult, WorkListReport> {

    override suspend fun run(params: WorkListReport): Either<Failure, SentReportResult> {

        val positions = mutableListOf<Position>()
        val checkResults = mutableListOf<CheckResult>()
        val marks = mutableListOf<Mark>()

        params.checksResults.filter { it.isNotMarkedGood() }.forEach { good ->
            positions.add(
                    Position(
                            matNr = good.material,
                            isProcessed = good.isProcessed.toSapBooleanString(),
                            quantity = good.getTotalQuantity()
                    )
            )

            good.scanResults.map { result ->
                checkResults.add(
                        CheckResult(
                                matNr = good.material,
                                quantity = result.quantity,
                                comment = result.commentCode,
                                producedDate = result.productionDate.getFormattedDate(),
                                shelfLife = result.expirationDate.getFormattedDate()
                        )
                )
            }
        }

        params.checksResults.filter { !it.isNotMarkedGood() }.forEach { good ->
            good.marks.map { markNumber ->
                marks.add(
                        Mark(
                                matNr = good.material,
                                markNumber = markNumber
                        )
                )
            }
        }

        return fmpRequestsHelper.restRequest("ZMP_UTZ_WKL_06_V001",
                FmpReport(
                        ip = params.ip,
                        description = params.description.taskName,
                        taskNumber = params.description.taskNumber,
                        tkNumber = params.description.tkNumber,
                        isNotFinished = params.isNotFinish.toSapBooleanString(),
                        positions = positions,
                        checkResults = checkResults,
                        marks = marks
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


data class WorkListReport(
        val ip: String,
        val description: WorkListTaskDescription,
        val isNotFinish: Boolean,
        val checksResults: List<Good>
)

data class FmpReport(
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val ip: String,
        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        /** Название задания */
        @SerializedName("IV_DESCR")
        val description: String,
        /** Номер ТК */
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        /** Обработка задания не закончена */
        @SerializedName("IV_NOT_FINISH")
        val isNotFinished: String,
        /** Таблица состава задания */
        @SerializedName("IT_TASK_POS")
        val positions: List<Position>,
        /** Таблица результаты проверки */
        @SerializedName("IT_CHECK_RESULT")
        val checkResults: List<CheckResult>,
        /** Таблица марок задания */
        @SerializedName("IT_TASK_MARK")
        val marks: List<Mark>
)