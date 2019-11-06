package com.lenta.bp14.requests.check_list

import com.google.gson.annotations.SerializedName
import com.lenta.bp14.models.check_list.CheckListTaskDescription
import com.lenta.bp14.models.check_list.Good
import com.lenta.bp14.requests.pojo.Position
import com.lenta.bp14.requests.pojo.ReportSentStatus
import com.lenta.bp14.requests.pojo.SentReportResult
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.Uom
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.utilities.extentions.toSapBooleanString
import javax.inject.Inject

class CheckListSendReportNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<SentReportResult, CheckListReport> {

    override suspend fun run(params: CheckListReport): Either<Failure, SentReportResult> {

        val checkPositions = mutableListOf<Position>()

        params.checksResults.forEach { good ->
            val quantity = good.quantity.value?.toDoubleOrNull() ?: 0.0
            checkPositions.add(
                    Position(
                            matNr = good.material,
                            isProcessed = true.toSapBooleanString(),
                            quantity = if (good.defaultUnits == Uom.G) quantity * 1000 else quantity
                    )
            )
        }

        return fmpRequestsHelper.restRequest("ZMP_UTZ_WKL_09_V001",
                FmpReport(
                        ip = params.ip,
                        description = params.description.taskName,
                        tkNumber = params.description.tkNumber,
                        isNotFinished = params.isNotFinish.toSapBooleanString(),
                        positions = checkPositions
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


data class CheckListReport(
        val ip: String,
        val description: CheckListTaskDescription,
        val isNotFinish: Boolean,
        val checksResults: List<Good>
)


data class FmpReport(
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val ip: String,
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
        val positions: List<Position>
)