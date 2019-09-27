package com.lenta.bp14.requests.not_exposed_product

import com.lenta.bp14.fmp.resources.ZmpUtzWkl08V001Rfc
import com.lenta.bp14.fmp.resources.ZmpUtzWkl08V001Rfc.*
import com.lenta.bp14.fmp.resources.ZmpUtzWkl08V001Rfc.LimitedScalarParameter.*
import com.lenta.bp14.models.not_exposed_products.NotExposedProductsTaskDescription
import com.lenta.bp14.models.not_exposed_products.repo.INotExposedProductInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.extentions.hhive.toEitherBoolean
import com.lenta.shared.utilities.extentions.toSapBooleanString
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class NotExposedSendReportNetRequest
@Inject constructor(hyperHive: HyperHive) : UseCase<SentReportResult, NotExposedReport>() {

    private val zmpUtzWkl08V001Rfc by lazy { ZmpUtzWkl08V001Rfc(hyperHive) }

    override suspend fun run(params: NotExposedReport): Either<Failure, SentReportResult> {

        val checkPlaces = mutableListOf<Param_IT_CHECK_PLACE>()
        val checkPositions = mutableListOf<Param_IT_TASK_POS>()

        params.checksResults.forEach {

            checkPlaces.add(
                    Param_IT_CHECK_PLACE(
                            it.matNr,
                            when (it.isEmptyPlaceMarked) {
                                false -> "3"
                                true -> "2"
                                else -> "1"
                            }
                    )
            )

            if (it.isEmptyPlaceMarked == null) {
                checkPositions.add(
                        Param_IT_TASK_POS(
                                it.matNr,
                                true.toSapBooleanString(),
                                it.quantity
                        )
                )
            }
        }

        return zmpUtzWkl08V001Rfc.newRequest()
                .addScalar(IV_IP(params.ip))
                .addScalar(IV_NOT_FINISH(params.isNotFinish.toSapBooleanString()))
                .addScalar(IV_TASK_NUM(params.description.taskNumber))
                .addScalar(IV_WERKS(params.description.tkNumber))
                .addScalar(IV_DESCR(params.description.taskName))
                .addTableItems(checkPlaces)
                .addTableItems(checkPositions)
                .streamCallTable().execute().toEitherBoolean("ZMP_UTZ_WKL_08_V001_RFC").rightToLeft {
                    @Suppress("INACCESSIBLE_TYPE")
                    zmpUtzWkl08V001Rfc.localHelper_ET_RETCODE.all.filter { it.retcode == 1 }.getOrNull(0)?.let {
                        Failure.SapError(it.errorText)
                    }
                }.map {
                    SentReportResult(
                            createdTasks = zmpUtzWkl08V001Rfc.localHelper_ET_TASK_LIST.all.map {
                                CreatedTaskInfo(
                                        taskNumber = it.taskNum,
                                        text1 = it.text1,
                                        text2 = it.text2
                                )
                            }
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

data class SentReportResult(
        val createdTasks: List<CreatedTaskInfo>
)


data class CreatedTaskInfo(
        val taskNumber: String,
        val text1: String,
        val text2: String
)


