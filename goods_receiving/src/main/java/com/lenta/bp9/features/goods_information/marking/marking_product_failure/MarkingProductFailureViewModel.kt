package com.lenta.bp9.features.goods_information.marking.marking_product_failure

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.model.processing.ProcessMarkingProductService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.requests.network.RejectNetRequest
import com.lenta.bp9.requests.network.RejectRequestParameters
import com.lenta.bp9.requests.network.RejectRequestResult
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.toStringFormatted
import kotlinx.coroutines.launch
import javax.inject.Inject

class MarkingProductFailureViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var processMarkingProductService: ProcessMarkingProductService

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var context: Context

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()

    val tvMessage: MutableLiveData<String> by lazy {
        val origQuantity =
                productInfo.value
                        ?.origQuantity
                        ?.toDouble()
                        .toStringFormatted()

        val notProcessedNumberOfStamps =
                productInfo.value
                        ?.let { productInfo ->
                            taskManager.getReceivingTask()
                                    ?.taskRepository
                                    ?.getBlocksDiscrepancies()
                                    ?.notProcessedNumberOfStampsByProduct(productInfo)
                                    .toStringFormatted()
                        }.orEmpty()

        MutableLiveData(context.getString(R.string.marking_product_rejection_dialogue, notProcessedNumberOfStamps, origQuantity))
    }

    val enabledPartialFailureBtn: MutableLiveData<Boolean> by lazy {
        /** https://trello.com/c/vcymT9Kp
         * Частичный отказ. Кнопка доступна для нажатия, если обработана хотя бы одна марка по товару.
         */
        MutableLiveData(
                (taskManager.getReceivingTask()
                        ?.taskRepository
                        ?.getBlocksDiscrepancies()
                        ?.findBlocksDiscrepanciesOfProduct(productInfo.value!!)
                        ?.filter { blockDiscrepancies ->
                            blockDiscrepancies.isScan
                        }?.size
                        ?: 0
                        ) > 0
        )
    }

    private val paramGrzGrundMarkCode: MutableLiveData<String> = MutableLiveData("")
    private val paramGrzGrundMarkName: MutableLiveData<String> = MutableLiveData("")

    init {
        launchUITryCatch {
            paramGrzGrundMarkCode.value = dataBase.getGrzGrundMark().orEmpty()
            paramGrzGrundMarkName.value = dataBase.getGrzGrundMarkName(paramGrzGrundMarkCode.value.orEmpty()).orEmpty()
        }
    }

    fun onClickCompleteRejection() {
        productInfo.value
                ?.let { productInfo ->
                    if (processMarkingProductService.newProcessMarkingProductService(productInfo) == null) {
                        screenNavigator.openAlertWrongProductType()
                    } else {
                        screenNavigator.openCompleteRejectionOfMarkingGoodsDialog(
                                applyCallbackFunc = {
                                    screenNavigator.goBack()
                                    processMarkingProductService.denialOfFullProductAcceptance(paramGrzGrundMarkCode.value.orEmpty())
                                },
                                title = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                countBlocks = productInfo.origQuantity.toDouble().toStringFormatted(),
                                paramGrzGrundMarkName = paramGrzGrundMarkName.value.orEmpty()
                        )
                    }
                }
    }

    fun onClickPartialFailure() {
        productInfo.value
                ?.let { productInfo ->
                    if (processMarkingProductService.newProcessMarkingProductService(productInfo) == null) {
                        screenNavigator.openAlertWrongProductType()
                    } else {
                        val processedNumberOfStamps =
                                taskManager.getReceivingTask()
                                        ?.taskRepository
                                        ?.getBlocksDiscrepancies()
                                        ?.processedNumberOfStampsByProduct(productInfo)
                                        ?.toDouble()
                                        .toStringFormatted()

                        val notProcessedNumberOfStamps =
                                taskManager.getReceivingTask()
                                        ?.taskRepository
                                        ?.getBlocksDiscrepancies()
                                        ?.notProcessedNumberOfStampsByProduct(productInfo)
                                        .toStringFormatted()
                        screenNavigator.openPartialRefusalOnMarkingGoodsDialog(
                                applyCallbackFunc = {
                                    screenNavigator.goBack()
                                    processMarkingProductService.refusalToAcceptPartlyByProduct(paramGrzGrundMarkCode.value.orEmpty())
                                },
                                title = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                countScanBlocks = processedNumberOfStamps,
                                unconfirmedQuantity = notProcessedNumberOfStamps,
                                paramGrzGrundMarkName = paramGrzGrundMarkName.value.orEmpty()
                        )
                    }
                }
    }
}
