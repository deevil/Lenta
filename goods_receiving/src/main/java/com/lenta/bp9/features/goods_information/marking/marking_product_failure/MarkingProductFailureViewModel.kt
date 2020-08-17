package com.lenta.bp9.features.goods_information.marking.marking_product_failure

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.model.processing.*
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.MarkingGoodsRegime
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.getMarkingGoodsRegime
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.requests.network.RejectNetRequest
import com.lenta.bp9.requests.network.RejectRequestParameters
import com.lenta.bp9.requests.network.RejectRequestResult
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.asyncLiveData
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.orIfNull
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class MarkingProductFailureViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var processMarkingProductService: ProcessMarkingProductService

    @Inject
    lateinit var processMarkingBoxProductService: ProcessMarkingBoxProductService

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var context: Context

    private val taskRepository by lazy { taskManager.getReceivingTask()?.taskRepository }
    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()

    private val processMarkingProduct by lazy {
        productInfo.value
                ?.let {
                    when(getMarkingGoodsRegime(taskManager, it)) {
                        MarkingGoodsRegime.UomStWithoutBoxes -> processMarkingProductService.newProcessMarkingProductService(it)
                        MarkingGoodsRegime.UomStWithBoxes -> processMarkingBoxProductService.newProcessMarkingProductService(it)
                        else -> null
                    }
                }
    }

    val tvMessage: MutableLiveData<String> by lazy {
        val confirmedByScanning =
                productInfo.value
                        ?.let { processMarkingProduct?.getConfirmedByScanning() }
                        ?: 0.0

        val productOrigQuantity =
                productInfo.value
                        ?.origQuantity
                        ?.toDouble()
                        ?: 0.0

        val notConfirmedByScanning = productOrigQuantity - confirmedByScanning

        val unit =
                productInfo.value
                        ?.purchaseOrderUnits
                        ?.name
                        ?.toLowerCase(Locale.getDefault())
                        .orEmpty()
        MutableLiveData(context.getString(R.string.marking_product_rejection_dialogue, notConfirmedByScanning.toStringFormatted(), "${productOrigQuantity.toStringFormatted()} $unit"))
    }

    val enabledPartialFailureBtn: MutableLiveData<Boolean> by lazy {
        /** https://trello.com/c/vcymT9Kp
         * Частичный отказ. Кнопка доступна для нажатия, если обработана хотя бы одна марка по товару.
         */
        MutableLiveData(
                productInfo.value
                        ?.let {
                            (taskRepository
                                    ?.run {
                                        getBlocksDiscrepancies()
                                                .findBlocksDiscrepanciesOfProduct(it)
                                                .filter { blockDiscrepancies -> blockDiscrepancies.isScan }
                                                .size
                                    }
                                    ?: 0
                                    ) > 0
                        }
                        .orIfNull { false }
        )
    }

    private val paramGrzGrundMarkCode: MutableLiveData<String> = MutableLiveData("")
    private val paramGrzGrundMarkName: MutableLiveData<String> = MutableLiveData("")

    init {
        launchUITryCatch {
            productInfo.value
                    ?.let {
                        if (processMarkingProduct == null) {
                            screenNavigator.goBackAndShowAlertWrongProductType()
                            return@launchUITryCatch
                        }
                    }.orIfNull {
                        screenNavigator.goBackAndShowAlertWrongProductType()
                        return@launchUITryCatch
                    }

            paramGrzGrundMarkCode.value = dataBase.getGrzGrundMark().orEmpty()
            paramGrzGrundMarkName.value = dataBase.getGrzGrundMarkName(paramGrzGrundMarkCode.value.orEmpty()).orEmpty()
        }
    }

    fun onClickCompleteRejection() {
        productInfo.value
                ?.let { productInfo ->
                    if (processMarkingProduct?.newProcessMarkingProductService(productInfo) == null) {
                        screenNavigator.openAlertWrongProductType()
                    } else {
                        val productOrigQuantity = productInfo.origQuantity.toDouble().toStringFormatted()
                        val unitName = productInfo.purchaseOrderUnits.name.toLowerCase(Locale.getDefault())
                        screenNavigator.openCompleteRejectionOfMarkingGoodsDialog(
                                nextCallbackFunc = {
                                    screenNavigator.goBack()
                                    processMarkingProduct?.denialOfFullProductAcceptance(paramGrzGrundMarkCode.value.orEmpty())
                                },
                                title = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                productOrigQuantity = "$productOrigQuantity $unitName",
                                paramGrzGrundMarkName = paramGrzGrundMarkName.value.orEmpty()
                        )
                    }
                }
    }

    fun onClickPartialFailure() {
        productInfo.value
                ?.let { productInfo ->
                    if (processMarkingProduct?.newProcessMarkingProductService(productInfo) == null) {
                        screenNavigator.openAlertWrongProductType()
                    } else {
                        val confirmedByScanning = processMarkingProduct?.getConfirmedByScanning() ?: 0.0
                        val notConfirmedByScanning = productInfo.origQuantity.toDouble() - confirmedByScanning
                        val unitName = productInfo.purchaseOrderUnits.name.toLowerCase(Locale.getDefault())
                        screenNavigator.openPartialRefusalOnMarkingGoodsDialog(
                                nextCallbackFunc = {
                                    screenNavigator.goBack()
                                    processMarkingProduct?.refusalToAcceptPartlyByProduct(paramGrzGrundMarkCode.value.orEmpty())
                                },
                                title = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                confirmedByScanning = "${confirmedByScanning.toStringFormatted()} $unitName",
                                notConfirmedByScanning = "${notConfirmedByScanning.toStringFormatted()} $unitName",
                                paramGrzGrundMarkName = paramGrzGrundMarkName.value.orEmpty()
                        )
                    }
                }
    }
}
