package com.lenta.bp14.requests.not_exposed_product

import com.google.gson.annotations.SerializedName
import com.lenta.bp14.models.not_exposed_products.NotExposedProductsTaskDescription
import com.lenta.bp14.models.not_exposed_products.repo.INotExposedProductInfo
import com.lenta.bp14.requests.pojo.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.utilities.extentions.toSapBooleanString
import javax.inject.Inject

class NotExposedSendReportNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<SentReportResult, NotExposedReport> {

    override suspend fun run(params: NotExposedReport): Either<Failure, SentReportResult> {

        val checkPlaces = mutableListOf<CheckPlace>()
        val checkPositions = mutableListOf<Position>()

        params.checksResults.forEach {

            checkPlaces.add(
                    CheckPlace(
                            matNr = it.matNr,
                            statCheck = when (it.isEmptyPlaceMarked) {
                                false -> "3"
                                true -> "2"
                                else -> "1"
                            }
                    )
            )

            checkPositions.add(
                    Position(
                            matNr = it.matNr,
                            isProcessed = (it.isEmptyPlaceMarked != null).toSapBooleanString(),
                            quantity = it.quantity ?: 0.0
                    )
            )
        }

        return fmpRequestsHelper.restRequest("ZMP_UTZ_WKL_08_V001",
                SapReport(
                        description = params.description.taskName,
                        ip = params.ip,
                        isNotFinished = params.isNotFinish.toSapBooleanString(),
                        taskNumber = params.description.taskNumber,
                        tkNumber = params.description.tkNumber,
                        positions = checkPositions,
                        places = checkPlaces
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


data class NotExposedReport(
        val ip: String,
        val description: NotExposedProductsTaskDescription,
        val isNotFinish: Boolean,
        val checksResults: List<INotExposedProductInfo>

)


data class SapReport(
        @SerializedName("IV_DESCR")
        val description: String,

        @SerializedName("IV_IP")
        val ip: String,

        @SerializedName("IV_NOT_FINISH")
        val isNotFinished: String,

        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,

        @SerializedName("IV_WERKS")
        val tkNumber: String,

        @SerializedName("IT_TASK_POS")
        val positions: List<Position>,

        @SerializedName("IT_CHECK_PLACE")
        val places: List<CheckPlace>
)


