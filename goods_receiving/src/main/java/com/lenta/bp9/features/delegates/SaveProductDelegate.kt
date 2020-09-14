package com.lenta.bp9.features.delegates

import android.content.Context
import com.lenta.bp9.features.base.IBaseTaskManager
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.platform.navigation.ScreenNavigatorPageNumberConstant.PAGE_NUMBER_97
import com.lenta.bp9.requests.network.EndRecountDDParameters
import com.lenta.bp9.requests.network.EndRecountDDResult
import com.lenta.bp9.requests.network.EndRecountDirectDeliveriesNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.utilities.extentions.getDeviceIp
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

interface ISaveProductDelegate : CoroutineScope, IBaseTaskManager {
    fun saveDataInERP()
}

class SaveProductDelegate
@Inject constructor(
        override val taskManager: IReceivingTaskManager,
        private val screenNavigator: IScreenNavigator,
        private val sessionInfo: ISessionInfo,
        private val context: Context,
        private val endRecountDirectDeliveries: EndRecountDirectDeliveriesNetRequest
):  ISaveProductDelegate {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    private fun isZBatchesNeedPrint(): Boolean {
        return (receivingTask
                ?.getProcessedProducts()
                ?.filter {
                    it.isZBatches
                            && !it.isVet
                            && it.isNeedPrint
                }
                ?.map { zBatch ->
                    receivingTask
                            ?.getProcessedProductsDiscrepancies()
                            ?.findLast {
                                it.materialNumber == zBatch.materialNumber
                                        && it.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                            }
                }
                ?.size
                ?: 0) > 0
    }

    override fun saveDataInERP() {
        if (isZBatchesNeedPrint()) {
            screenNavigator.openSaveCountedQuantitiesAndGoToLabelPrintingDialog(
                    yesCallbackFunc = {
                        launch {
                            /**todo delay (500) это временное решение, т.к. без задержки в ф-ции dataTransferToServer из-за этого не отображается screenNavigator.showProgressLoadingData()
                             * todo в итоге не видно процесс загрузки
                             */
                            delay(500)
                            removingTemporaryDataFromRepository()
                            dataTransferToServer()
                        }
                    }
            )
        } else {
            removingTemporaryDataFromRepository()
            dataTransferToServer()
        }
    }

    private fun filterClearTabTaskDiff(productInfo: TaskProductInfo) : Boolean {
        //партионный - это помеченный IS_ALCO и не помеченный IS_BOX_FL, IS_MARK_FL (Артем)
        val isBatchNonExciseAlcoholProduct =
                productInfo.type == ProductType.NonExciseAlcohol
                        && !productInfo.isBoxFl
                        && !productInfo.isMarkFl

        val isVetProduct = productInfo.isVet && !productInfo.isNotEdit

        //коробочный или марочный алкоголь
        val isExciseAlcoholProduct =
                productInfo.type == ProductType.ExciseAlcohol
                        && (productInfo.isBoxFl || productInfo.isMarkFl)

        return isBatchNonExciseAlcoholProduct
                || isVetProduct
                || isExciseAlcoholProduct
    }

    private fun removingTemporaryDataFromRepository() {
        /**
         * очищаем таблицу ET_TASK_DIFF от не акцизного (партионного) алкоголя и веттоваров,
         * т.к. для партионного товара необходимо передавать только данные из таблицы ET_PARTS_DIFF, а для веттоваров - ET_VET_DIFF,
         */
        receivingTask
                ?.getProcessedProductsDiscrepancies()
                ?.mapNotNull { productDiscr ->
                    taskRepository
                            ?.getProducts()
                            ?.findProduct(productDiscr.materialNumber)
                }
                ?.filter { filterClearTabTaskDiff(it) }
                ?.forEach { productForDel ->
                    taskRepository
                            ?.getProductsDiscrepancies()
                            ?.deleteProductsDiscrepanciesForProduct(productForDel.materialNumber)
                }
    }

    private fun dataTransferToServer() {
        launch {
            screenNavigator.showProgressLoadingData()
            endRecountDirectDeliveries(
                    EndRecountDDParameters(
                            taskNumber = taskNumber,
                            deviceIP = context.getDeviceIp(),
                            personalNumber = sessionInfo.personnelNumber.orEmpty(),
                            discrepanciesProduct = processedProductsDiscrepancies?.map { TaskProductDiscrepanciesRestData.from(it) }.orEmpty(),
                            discrepanciesBatches = processedBatchesDiscrepancies?.map { TaskBatchesDiscrepanciesRestData.from(it) }.orEmpty(),
                            discrepanciesBoxes = processedBoxesDiscrepancies?.map { TaskBoxDiscrepanciesRestData.from(it) }.orEmpty(),
                            discrepanciesExciseStamp = processedExciseStampsDiscrepancies?.map { TaskExciseStampDiscrepanciesRestData.from(it) }.orEmpty(),
                            exciseStampBad = processedExciseStampsBad?.map { TaskExciseStampBadRestData.from(it) }.orEmpty(),
                            discrepanciesMercury = processedMercuryDiscrepancies?.map { TaskMercuryDiscrepanciesRestData.from(it) }.orEmpty(),
                            discrepanciesBlocks = processedBlocksDiscrepancies?.map { TaskBlockDiscrepanciesRestData.from(it) }.orEmpty(),
                            discrepanciesZBatches = processedZBatchesDiscrepancies?.map { TaskZBatchesDiscrepanciesRestData.from(it) }.orEmpty()
                    )
            ).either(::handleFailure, ::handleSuccessSavedData)
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccessSavedData(result: EndRecountDDResult) {
        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
        if (isZBatchesNeedPrint()) {
            screenNavigator.openLabelPrintingScreen()
        } else {
            screenNavigator.openTaskCardScreen(TaskCardMode.Full, taskType)
        }

    }

    private fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure, PAGE_NUMBER_97)
    }

}