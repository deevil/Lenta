package com.lenta.bp9.features.goods_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.EndRecountDDParameters
import com.lenta.bp9.requests.network.EndRecountDDResult
import com.lenta.bp9.requests.network.EndRecountDirectDeliveriesNetRequest
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

    val selectedPage = MutableLiveData(0)
    val countedSelectionsHelper = SelectionItemsHelper()
    val listCounted: MutableLiveData<List<ListCountedItem>> = MutableLiveData()
    val listWithoutBarcode: MutableLiveData<List<ListWithoutBarcodeItem>> = MutableLiveData()
    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()
    private val isBatches: MutableLiveData<Boolean> = MutableLiveData(false)

    val visibilityCleanButton: MutableLiveData<Boolean> = selectedPage.map {
        it == 0
    }

    val enabledCleanButton: MutableLiveData<Boolean> = countedSelectionsHelper.selectedPositions.map {
        val selectedComponentsPositions = countedSelectionsHelper.selectedPositions.value
        !selectedComponentsPositions.isNullOrEmpty()
    }

    val visibilityBatchesButton: MutableLiveData<Boolean> = MutableLiveData()

    init {
        viewModelScope.launch {
            searchProductDelegate.init(viewModelScope = this@GoodsListViewModel::viewModelScope,
                    scanResultHandler = this@GoodsListViewModel::handleProductSearchResult)
        }
    }

    fun onResume() {
        visibilityBatchesButton.value = taskManager.getReceivingTask()?.taskDescription?.isAlco
        updateListCounted()
        updateListWithoutBarcode()
    }

    private fun updateListCounted() {
        taskManager.getReceivingTask()?.let { task ->
            if (!isBatches.value!!) {
                listCounted.postValue(
                        task.getProcessedProducts()
                                .filter {
                                    (task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(it) +
                                            task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(it)) > 0.0
                                }
                                .mapIndexed { index, productInfo ->
                                    val acceptTotalCount = task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo)
                                    val acceptTotalCountWithUom = if (acceptTotalCount != 0.0) {
                                        "+ ${acceptTotalCount.toStringFormatted()} ${productInfo.uom.name}"
                                    } else {
                                        "0 ${productInfo.uom.name}"
                                    }
                                    val refusalTotalCount = task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(productInfo)
                                    val refusalTotalCountWithUom = if (refusalTotalCount != 0.0) {
                                        "- ${refusalTotalCount.toStringFormatted()} ${productInfo.uom.name}"
                                    } else {
                                        "0 ${productInfo.uom.name}"
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
                                    it.isNoEAN && (task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(it) +
                                            task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(it) == 0.0)
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

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    fun onScanResult(data: String) {
        searchProductDelegate.searchCode(code = data, fromScan = true)
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
            listCounted.value?.get(position)?.productInfo?.materialNumber
        } else {
            listWithoutBarcode.value?.get(position)?.productInfo?.materialNumber
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

    fun onClickClean() {
        if (!isBatches.value!!) {
            countedSelectionsHelper.selectedPositions.value?.map { position ->
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
        } else {
            countedSelectionsHelper.selectedPositions.value?.map { position ->
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getBatchesDiscrepancies()
                        ?.deleteBatchesDiscrepanciesForBatch(listCounted.value?.get(position)!!.batchInfo!!)
            }
        }

        updateListCounted()
        updateListWithoutBarcode()
    }

    fun onClickBatches() {
        isBatches.value = !isBatches.value!!
        updateListCounted()
        updateListWithoutBarcode()
    }

    fun onClickSave() {
        viewModelScope.launch {
            if (taskManager.getReceivingTask()!!.taskRepository.getProducts().getProducts().size > (listCounted.value?.size ?: 0)) {
                screenNavigator.openDiscrepancyListScreen()
            } else {
                screenNavigator.showProgressLoadingData()
                endRecountDirectDeliveries(EndRecountDDParameters(
                        taskNumber = taskManager.getReceivingTask()!!.taskHeader.taskNumber,
                        deviceIP = context.getDeviceIp(),
                        personalNumber = sessionInfo.personnelNumber ?: "",
                        discrepanciesProduct = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getProductsDiscrepancies().map { TaskProductDiscrepanciesRestData.from(it) },
                        discrepanciesBatches = taskManager.getReceivingTask()!!.taskRepository.getBatchesDiscrepancies().getBatchesDiscrepancies().map { TaskBatchesDiscrepanciesRestData.from(it) },
                        discrepanciesMercury = taskManager.getReceivingTask()!!.taskRepository.getMercuryDiscrepancies().getMercuryDiscrepancies().map { TaskMercuryDiscrepanciesRestData.from(it) }
                )).either(::handleFailure, ::handleSuccess)
                screenNavigator.hideProgress()
            }
        }
    }

    private fun handleSuccess(result: EndRecountDDResult) {
        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
        screenNavigator.openTaskCardScreen(TaskCardMode.Full)
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
