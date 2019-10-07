package com.lenta.bp14.requests.work_list

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
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.utilities.extentions.toSapBooleanString
import javax.inject.Inject

class DeliveriesNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<SentReportResult, CheckListReport> {

    override suspend fun run(params: CheckListReport): Either<Failure, SentReportResult> {

        val checkPositions = mutableListOf<Position>()

        params.checksResults.forEach {
            checkPositions.add(
                    Position(
                            matNr = it.material,
                            isProcessed = true.toSapBooleanString(),
                            quantity = it.quantity.value?.toDoubleOrNull() ?: 0.0
                    )
            )
        }

        return fmpRequestsHelper.restRequest("ZMP_UTZ_WKL_13_V001",
                FmpRequest(
                        ip = params.ip,
                        description = params.description.taskName
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

data class PlannedDeliveries(
        // Тип поставки
        @SerializedName("TYPE_DELIV")
        val type: String,

        // Статус поставки
        @SerializedName("STAT_DELIV")
        val status: String,

        // Количество в заказе
        @SerializedName("MENGE")
        val quantityInOrder: String,

        // Количество в входящей поставке
        @SerializedName("ORMNG")
        val quantityInDelivery: String,

        // ЕИ заказа на поставку
        @SerializedName("BSTME")
        val unitsCode: String,

        // Планируемая дата поставки
        @SerializedName("DATE_PLAN")
        val date: String,

        // Планируемое время поставки
        @SerializedName("TIME_PLAN")
        val time: String
)

data class FmpRequest(
        // Номер ТК
        @SerializedName("IV_WERKS")
        val ip: String,

        // Номер товара
        @SerializedName("IV_MATNR")
        val description: String
)