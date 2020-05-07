package com.lenta.bp9.features.goods_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var endRecountDirectDeliveries: EndRecountDirectDeliveriesNetRequest
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate
    @Inject
    lateinit var hyperHive: HyperHive

    val selectedPage = MutableLiveData(0)
    val countedSelectionsHelper = SelectionItemsHelper()
    val toProcessingSelectionsHelper = SelectionItemsHelper()
    val processedSelectionsHelper = SelectionItemsHelper()
    val listCounted: MutableLiveData<List<ListCountedItem>> = MutableLiveData()
    val listWithoutBarcode: MutableLiveData<List<ListWithoutBarcodeItem>> = MutableLiveData()
    val listToProcessing: MutableLiveData<List<ListShipmentPPItem>> = MutableLiveData()
    val listProcessed: MutableLiveData<List<ListShipmentPPItem>> = MutableLiveData()
    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()
    val taskType: MutableLiveData<TaskType> = MutableLiveData()
    private val isBatches: MutableLiveData<Boolean> = MutableLiveData(false)

    val visibilityCleanButton: MutableLiveData<Boolean> = selectedPage.map {
        it == 0
    }

    val enabledCleanButton: MutableLiveData<Boolean> = countedSelectionsHelper.selectedPositions.map {
        !it.isNullOrEmpty()
    }

    val visibilityBatchesButton: MutableLiveData<Boolean> by lazy {
        MutableLiveData(taskManager.getReceivingTask()?.taskDescription?.isAlco == true && !(taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.ShipmentPP || taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.ShipmentRC)) //для заданий ОПП и ОРЦ не показываем кнопку Партия, уточнил у Артема
    }

    val enabledBtnSaveForShipmentPP: MutableLiveData<Boolean> = listToProcessing.map {
        it?.isEmpty()
    }

    val enabledBtnMissingForShipmentPP = toProcessingSelectionsHelper.selectedPositions.map {
        !it.isNullOrEmpty()
    }

    val enabledBtnCleanForShipmentPP = processedSelectionsHelper.selectedPositions.map {
        !it.isNullOrEmpty()
    }

    val enabledBtnSkipForShipmentPP: MutableLiveData<Boolean> by lazy {
        MutableLiveData(taskManager.getReceivingTask()?.taskDescription?.isSkipCountMan == true)
    }

    val isTaskPGE: MutableLiveData<Boolean> by lazy {
        MutableLiveData(taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit)
    }

    init {
        viewModelScope.launch {
            searchProductDelegate.init(viewModelScope = this@GoodsListViewModel::viewModelScope,
                    scanResultHandler = this@GoodsListViewModel::handleProductSearchResult)
        }
    }

    fun onResume() {
        updateData()
    }

    private fun updateData() {
        if (taskType.value == TaskType.ShipmentPP) {
            updateListToProcessing()
            updateListProcessed()
        } else {
            updateListCounted()
            updateListWithoutBarcode()
        }
    }

    private fun updateListCounted() {
        taskManager.getReceivingTask()?.let { task ->
            if (!isBatches.value!!) {
                listCounted.postValue(
                        task.getProcessedProducts()
                                .filter {
                                    if (task.taskHeader.taskType == TaskType.RecalculationCargoUnit) {
                                        (task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(it) +
                                                task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProductPGE(it)) > 0.0
                                    } else {
                                        (task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(it) +
                                                task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(it)) > 0.0
                                    }
                                }
                                .mapIndexed { index, productInfo ->
                                    val uom = if (task.taskHeader.taskType == TaskType.DirectSupplier) {
                                        productInfo.purchaseOrderUnits
                                    } else {
                                        productInfo.uom
                                    }
                                    val acceptTotalCount = if (task.taskHeader.taskType == TaskType.RecalculationCargoUnit) {
                                        task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(productInfo)
                                    } else {
                                        task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo)
                                    }
                                    val acceptTotalCountWithUom = if (acceptTotalCount != 0.0) {
                                        "+ ${acceptTotalCount.toStringFormatted()} ${uom.name}"
                                    } else {
                                        "0 ${uom.name}"
                                    }
                                    val refusalTotalCount = if (task.taskHeader.taskType == TaskType.RecalculationCargoUnit) {
                                        task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProductPGE(productInfo)
                                    } else {
                                        task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(productInfo)
                                    }
                                    val refusalTotalCountWithUom = if (refusalTotalCount != 0.0) {
                                        "- ${refusalTotalCount.toStringFormatted()} ${uom.name}"
                                    } else {
                                        "0 ${uom.name}"
                                    }

                                    ListCountedItem(
                                            number = index + 1,
                                            name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                            countAcceptWithUom = acceptTotalCountWithUom,
                                            countRefusalWithUom = refusalTotalCountWithUom,
                                            isNotEdit = productInfo.isNotEdit,
                                            productInfo = productInfo,
                                            batchInfo = null,
                                            even = index % 2 == 0
                                    )
                                }
                                .reversed())
            } else {
                /**listCounted.postValue(
                        task.getProcessedBatches()
                                .filter {
                                    task.taskRepository.getBatchesDiscrepancies().getCountAcceptOfBatch(it) > 0.0
                                            || task.taskRepository.getBatchesDiscrepancies().getCountAcceptOfBatch(it) > 0.0
                                }
                                .mapIndexed { index, batchInfo ->
                                    val acceptTotalCount = task.taskRepository.getBatchesDiscrepancies().getCountAcceptOfBatch(batchInfo)
                                    val acceptTotalCountWithUom = if (acceptTotalCount != 0.0) {
                                        "+ ${acceptTotalCount.toStringFormatted()} ${batchInfo.uom.name}"
                                    } else {
                                        "0 ${batchInfo.uom.name}"
                                    }
                                    val refusalTotalCount = task.taskRepository.getBatchesDiscrepancies().getCountRefusalOfBatch(batchInfo)
                                    val refusalTotalCountWithUom = if (refusalTotalCount != 0.0) {
                                        "- ${refusalTotalCount.toStringFormatted()} ${batchInfo.uom.name}"
                                    } else {
                                        "0 ${batchInfo.uom.name}"
                                    }
                                    ListCountedItem(
                                            number = index + 1,
                                            name = "${batchInfo.getMaterialLastSix()} ${batchInfo.description} \nДР-${batchInfo.bottlingDate} // ${batchInfo.manufacturer}",
                                            countAcceptWithUom = acceptTotalCountWithUom,
                                            countRefusalWithUom = refusalTotalCountWithUom,
                                            productInfo = null,
                                            batchInfo = batchInfo,
                                            even = index % 2 == 0)
                                }
                                .reversed())*/
            }

        }

        countedSelectionsHelper.clearPositions()
    }

    private fun updateListWithoutBarcode() {
        taskManager.getReceivingTask()?.let { task ->
            if (!isBatches.value!!) {
                listWithoutBarcode.postValue(
                        task.getProcessedProducts()
                                .filter {
                                    if (task.taskHeader.taskType == TaskType.RecalculationCargoUnit) {
                                        it.isNoEAN && (task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(it) +
                                                task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProductPGE(it) == 0.0)
                                    } else {
                                        it.isNoEAN && (task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(it) +
                                                task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(it) == 0.0)
                                    }
                                }.mapIndexed { index, productInfo ->
                                    ListWithoutBarcodeItem(
                                            number = index + 1,
                                            name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                            productInfo = productInfo,
                                            batchInfo = null,
                                            even = index % 2 == 0)
                                }
                                .reversed())
            } else {
                /**listWithoutBarcode.postValue(
                        task.getProcessedBatches()
                                .filter {
                                    it.isNoEAN && (task.taskRepository.getBatchesDiscrepancies().getCountAcceptOfBatch(it) +
                                            task.taskRepository.getBatchesDiscrepancies().getCountRefusalOfBatch(it) == 0.0)
                                }.mapIndexed { index, batchInfo ->
                                    ListWithoutBarcodeItem(
                                            number = index + 1,
                                            name = "${batchInfo.getMaterialLastSix()} ${batchInfo.description} \nДР-${batchInfo.bottlingDate} // ${batchInfo.manufacturer}",
                                            productInfo = null,
                                            batchInfo = batchInfo,
                                            even = index % 2 == 0)
                                }
                                .reversed())*/
            }
        }
    }

    private fun updateListToProcessing() {
        taskManager.getReceivingTask()?.let { task ->
            listToProcessing.postValue(
                    task.getProcessedProducts()
                            .filter {
                                task.taskRepository.getProductsDiscrepancies().getQuantityDiscrepanciesOfProduct(it) == 0
                            }.mapIndexed { index, productInfo ->
                                ListShipmentPPItem(
                                        number = index + 1,
                                        name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                        countWithUom = "${productInfo.origQuantity.toDouble().toStringFormatted()} ${productInfo.uom.name}",
                                        productInfo = productInfo,
                                        even = index % 2 == 0)
                            }
                            .reversed())
        }

        toProcessingSelectionsHelper.clearPositions()
    }

    private fun updateListProcessed() {
        taskManager.getReceivingTask()?.let { task ->
            listProcessed.postValue(
                    task.getProcessedProducts()
                            .filter {
                                task.taskRepository.getProductsDiscrepancies().getQuantityDiscrepanciesOfProduct(it) > 0
                            }.mapIndexed { index, productInfo ->
                                ListShipmentPPItem(
                                        number = index + 1,
                                        name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                        countWithUom = "${task.taskRepository.getProductsDiscrepancies().getAllCountDiscrepanciesOfProduct(productInfo).toStringFormatted()} ${productInfo.uom.name}",
                                        productInfo = productInfo,
                                        even = index % 2 == 0)
                            }
                            .reversed())
        }

        processedSelectionsHelper.clearPositions()
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    fun onScanResult(data: String) {
        searchProductDelegate.searchCode(code = data, fromScan = true, isBarCode = true)
    }

    fun onResult(code: Int?) {
        if (searchProductDelegate.handleResultCode(code)) {
            return
        }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickItemPosition(position: Int) {
        val matnr: String? = if (selectedPage.value == 0) {
            if (taskType.value == TaskType.ShipmentPP) {
                listToProcessing.value?.get(position)?.productInfo?.materialNumber
            } else {
                listCounted.value?.get(position)?.productInfo?.materialNumber
            }
        } else {
            if (taskType.value == TaskType.ShipmentPP) {
                listProcessed.value?.get(position)?.productInfo?.materialNumber
            } else {
                listWithoutBarcode.value?.get(position)?.productInfo?.materialNumber
            }
        }
        searchProductDelegate.searchCode(code = matnr ?: "", fromScan = false)
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        eanCode.postValue("")
        return false
    }

    override fun onOkInSoftKeyboard(): Boolean {
        eanCode.value?.let {
            searchProductDelegate.searchCode(it, fromScan = false)
        }
        return true
    }

    fun onClickRefusal() {
        screenNavigator.openRejectScreen()
    }

    fun onClickThirdBtn() {
        if (taskType.value == TaskType.ShipmentPP) {//https://trello.com/c/3WVovfmE
            screenNavigator.openSkipRecountScreen()
        } else {
            if (!isBatches.value!!) {
                countedSelectionsHelper.selectedPositions.value?.map { position ->
                    val isNotRecountBreakingCargoUnit = isTaskPGE.value == true && taskManager.getReceivingTask()!!.taskHeader.isCracked && listCounted.value?.get(position)!!.productInfo!!.isWithoutRecount
                    if (isNotRecountBreakingCargoUnit) { //если это не пересчетная ГЕ //https://trello.com/c/PRTAVnUP
                        taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.deleteProductsDiscrepanciesForProductNotRecountPGE(listCounted.value?.get(position)!!.productInfo!!)
                    }
                    if (!listCounted.value?.get(position)!!.productInfo!!.isNotEdit && !isNotRecountBreakingCargoUnit) {
                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getProductsDiscrepancies()
                            ?.deleteProductsDiscrepanciesForProduct(listCounted.value?.get(position)!!.productInfo!!)

                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getMercuryDiscrepancies()
                            ?.deleteMercuryDiscrepanciesForProduct(listCounted.value?.get(position)!!.productInfo!!)
                    }
                }
            } else {
                countedSelectionsHelper.selectedPositions.value?.map { position ->
                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getBatchesDiscrepancies()
                            ?.deleteBatchesDiscrepanciesForBatch(listCounted.value?.get(position)!!.batchInfo!!)
                }
            }
            updateData()
        }
    }

    fun onClickFourthBtn() {
        if (taskType.value == TaskType.ShipmentPP) {//https://trello.com/c/3WVovfmE
            if (selectedPage.value == 0) {
                missingGoodsForShipmentPP()
            } else {
                cleanGoodsForShipmentPP()
            }
        } else {
            /**isBatches.value = !isBatches.value!!
            updateListCounted()
            updateListWithoutBarcode()*/
        }
    }

    private fun missingGoodsForShipmentPP() {
        toProcessingSelectionsHelper.selectedPositions.value?.map { position ->
            taskManager.getReceivingTask()?.
                    taskRepository?.
                    getProductsDiscrepancies()?.
                    changeProductDiscrepancy(TaskProductDiscrepancies(
                            materialNumber = listToProcessing.value?.get(position)!!.productInfo!!.materialNumber,
                            processingUnitNumber = listToProcessing.value?.get(position)!!.productInfo!!.processingUnit,
                            numberDiscrepancies = "0",
                            uom = listToProcessing.value?.get(position)!!.productInfo!!.uom,
                            typeDiscrepancies = "3",
                            isNotEdit = false,
                            isNew = false,
                            notEditNumberDiscrepancies = ""
                    ))
        }

        updateData()
    }

    private fun cleanGoodsForShipmentPP() {
        processedSelectionsHelper.selectedPositions.value?.map { position ->
            taskManager
                    .getReceivingTask()
                    ?.taskRepository
                    ?.getProductsDiscrepancies()
                    ?.deleteProductsDiscrepanciesForProduct(listProcessed.value?.get(position)!!.productInfo!!)

            taskManager
                    .getReceivingTask()
                    ?.taskRepository
                    ?.getMercuryDiscrepancies()
                    ?.deleteMercuryDiscrepanciesForProduct(listProcessed.value?.get(position)!!.productInfo!!)
        }

        updateData()
    }

    fun onClickSave() {
        viewModelScope.launch {
            val countProductNotProcessed = taskManager.getReceivingTask()?.let { task ->
                task.getProcessedProducts()
                        .map {productInfo ->
                            if (task.taskHeader.taskType == TaskType.RecalculationCargoUnit) {
                                task.taskRepository.getProductsDiscrepancies().getCountProductNotProcessedOfProductPGE(productInfo)
                            } else {
                                task.taskRepository.getProductsDiscrepancies().getCountProductNotProcessedOfProduct(productInfo)
                            }
                        }
            }?.sumByDouble {
                it
            } ?: 0.0

            if (countProductNotProcessed > 0.0) {
                screenNavigator.openDiscrepancyListScreen()
            } else {
                screenNavigator.showProgressLoadingData()
                endRecountDirectDeliveries(EndRecountDDParameters(
                        taskNumber = taskManager.getReceivingTask()!!.taskHeader.taskNumber,
                        deviceIP = context.getDeviceIp(),
                        personalNumber = sessionInfo.personnelNumber ?: "",
                        discrepanciesProduct = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getProductsDiscrepancies().map { TaskProductDiscrepanciesRestData.from(it) },
                        discrepanciesBatches = taskManager.getReceivingTask()!!.taskRepository.getBatchesDiscrepancies().getBatchesDiscrepancies().map { TaskBatchesDiscrepanciesRestData.from(it) },
                        discrepanciesBoxes = taskManager.getReceivingTask()!!.taskRepository.getBoxesDiscrepancies().getBoxesDiscrepancies().map { TaskBoxDiscrepanciesRestData.from(it) },
                        discrepanciesExciseStamp = taskManager.getReceivingTask()!!.taskRepository.getExciseStampsDiscrepancies().getExciseStampDiscrepancies().map { TaskExciseStampDiscrepanciesRestData.from(it) },
                        exciseStampBad = taskManager.getReceivingTask()!!.taskRepository.getExciseStampsBad().getExciseStampsBad().map { TaskExciseStampBadRestData.from(it) },
                        discrepanciesMercury = taskManager.getReceivingTask()!!.taskRepository.getMercuryDiscrepancies().getMercuryDiscrepancies().map { TaskMercuryDiscrepanciesRestData.from(it) }
                )).either(::handleFailure, ::handleSuccess)
                screenNavigator.hideProgress()
            }
        }
    }

    private fun handleSuccess(result: EndRecountDDResult) {
        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
        screenNavigator.openTaskCardScreen(TaskCardMode.Full, taskManager.getReceivingTask()?.taskHeader?.taskType ?: TaskType.None)
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure, pageNumber = "97")
    }

    fun onBackPressed() {
        screenNavigator.openUnsavedDataDialog(
                yesCallbackFunc = {
                    screenNavigator.openUnlockTaskLoadingScreen()
                }
        )
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToEan.value = true
        eanCode.value = eanCode.value ?: "" + digit
    }

}
