package com.lenta.bp9.features.goods_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.EndRecountDDParameters
import com.lenta.bp9.requests.network.EndRecountDDResult
import com.lenta.bp9.requests.network.EndRecountDirectDeliveriesNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.network.AuthParams
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
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

    val titleProgressScreen: MutableLiveData<String> = MutableLiveData()
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
                                    !it.isNoEAN
                                }
                                .mapIndexed { index, productInfo ->
                                    ListCountedItem(
                                            number = index + 1,
                                            name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                            countAccept = task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo),
                                            countRefusal = task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(productInfo),
                                            uomName = productInfo.uom.name,
                                            productInfo = productInfo,
                                            batchInfo = null,
                                            even = index % 2 == 0)
                                }
                                .reversed())
            } else {
                listCounted.postValue(
                        task.getProcessedBatches()
                                .filter {
                                    !it.isNoEAN
                                }
                                .mapIndexed { index, batchInfo ->
                                    ListCountedItem(
                                            number = index + 1,
                                            name = "${batchInfo.getMaterialLastSix()} ${batchInfo.description} \nДР-${batchInfo.bottlingDate} // ${batchInfo.manufacturer}",
                                            countAccept = task.taskRepository.getBatchesDiscrepancies().getCountAcceptOfBatch(batchInfo),
                                            countRefusal = task.taskRepository.getBatchesDiscrepancies().getCountRefusalOfBatch(batchInfo),
                                            uomName = batchInfo.uom.name,
                                            productInfo = null,
                                            batchInfo = batchInfo,
                                            even = index % 2 == 0)
                                }
                                .reversed())
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
                                    it.isNoEAN
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
                listWithoutBarcode.postValue(
                        task.getProcessedBatches()
                                .filter {
                                    it.isNoEAN
                                }.mapIndexed { index, batchInfo ->
                                    ListWithoutBarcodeItem(
                                            number = index + 1,
                                            name = "${batchInfo.getMaterialLastSix()} ${batchInfo.description} \nДР-${batchInfo.bottlingDate} // ${batchInfo.manufacturer}",
                                            productInfo = null,
                                            batchInfo = batchInfo,
                                            even = index % 2 == 0)
                                }
                                .reversed())
            }
        }
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    fun onScanResult(data: String) {
        //todo
        return
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickItemPosition(position: Int) {
        val matnr: String?
        if (selectedPage.value == 0) {
            matnr = listCounted.value?.get(position)?.productInfo?.materialNumber
        } else {
            matnr = listWithoutBarcode.value?.get(position)?.productInfo?.materialNumber
        }
        matnr?.let {
            val productInfo = taskManager.getReceivingTask()?.taskRepository?.getProducts()?.findProduct(it)
            if (productInfo != null) screenNavigator.openGoodsInfoScreen(productInfo)
        }
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToEan.value = true
        eanCode.value = eanCode.value ?: "" + digit
    }

    override fun onOkInSoftKeyboard(): Boolean {
        eanCode.value?.let {
            //todo
            //searchProductDelegate.searchCode(it, fromScan = false)
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
                taskManager.getReceivingTask()?.taskRepository?.getProducts()?.changeProduct(listCounted.value?.get(position)!!.productInfo!!.copy(isNoEAN = true))
            }
        } else {
            countedSelectionsHelper.selectedPositions.value?.map { position ->
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getBatchesDiscrepancies()
                        ?.deleteBatchesDiscrepanciesForBatch(listCounted.value?.get(position)!!.batchInfo!!)
                taskManager.getReceivingTask()?.taskRepository?.getBatches()?.changeBatch(listCounted.value?.get(position)!!.batchInfo!!.copy(isNoEAN = true))
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
            screenNavigator.showProgress(titleProgressScreen.value!!)
            if (taskManager.getReceivingTask()!!.getProcessedProducts().any { it.isNoEAN }) {
                screenNavigator.openDiscrepancyListScreen()
            } else {
                endRecountDirectDeliveries(EndRecountDDParameters(
                        taskNumber = taskManager.getReceivingTask()!!.taskHeader.taskNumber,
                        deviceIP = context.getDeviceIp(),
                        personalNumber = sessionInfo.personnelNumber ?: "",
                        discrepanciesProduct = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getProductsDiscrepancies(),
                        discrepanciesBatches = taskManager.getReceivingTask()!!.taskRepository.getBatchesDiscrepancies().getBatchesDiscrepancies()
                )).either(::handleFailure, ::handleSucess)
            }
            screenNavigator.hideProgress()
        }
    }

    private fun handleSucess(endRecountDDResult: EndRecountDDResult) {
        screenNavigator.goBack()
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure, pageNumber = "97")
    }

}
