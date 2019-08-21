package com.lenta.inventory.features.goods_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.R
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.StorePlaceLockMode
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.models.task.StorePlaceProcessing
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var lockRequest: StorePlaceLockNetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var taskManager: IInventoryTaskManager
    @Inject
    lateinit var dataSaver: DataSaver
    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    val unprocessedGoods: MutableLiveData<List<ProductInfoVM>> = MutableLiveData()
    val processedGoods: MutableLiveData<List<ProductInfoVM>> = MutableLiveData()
    var selectedPage = MutableLiveData(0)

    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    var storePlaceManager: StorePlaceProcessing? = null
    var justCreated: Boolean = true
    var lastPage: Int = 0

    val processedSelectionHelper = SelectionItemsHelper()
    val unprocessedSelectionHelper = SelectionItemsHelper()

    val deleteEnabled: MutableLiveData<Boolean> = selectedPage
            .combineLatest(unprocessedSelectionHelper.selectedPositions.combineLatest(processedSelectionHelper.selectedPositions))
            .combineLatest(unprocessedGoods.combineLatest(processedGoods))
            .map {
                val page = it?.first?.first
                if (page == 0) {
                    val selectedCount = it.first.second.first.size
                    val totalCount = it.second.first.size + it.second.second.size
                    selectedCount != 0 && selectedCount != totalCount
                } else {
                    val selectedCount = it?.first?.second?.second?.size
                    selectedCount != 0
                }
            }

    fun isStrict(): Boolean {
        return taskManager.getInventoryTask()?.taskDescription?.isStrict ?: false
    }

    init {
        viewModelScope.launch {
            dataSaver.setViewModelScopeFunc(::viewModelScope)
        }

    }

    fun getTitle(): String {
        when (taskManager.getInventoryTask()!!.taskDescription.recountType) {
            RecountType.Simple -> return "${taskManager.getInventoryTask()?.taskDescription?.getTaskTypeAndNumber()
                    ?: ""} / ${context.getString(R.string.simple_recount)}"
            RecountType.ParallelByStorePlaces -> return "${taskManager.getInventoryTask()?.taskDescription?.getTaskTypeAndNumber()
                    ?: ""} / МХ-${storePlaceManager?.storePlaceNumber}"
            RecountType.ParallelByPerNo -> return "${taskManager.getInventoryTask()?.taskDescription?.getTaskTypeAndNumber()
                    ?: ""} / ${sessionInfo.personnelFullName}"
        }
        return taskManager.getInventoryTask()?.taskDescription?.getTaskTypeAndNumber() ?: ""
    }

    fun setStorePlaceNumber(storePlaceNumber: String) {
        storePlaceManager = taskManager.getInventoryTask()!!.processStorePlace(storePlaceNumber = storePlaceNumber)
    }

    init {
        viewModelScope.launch {
            searchProductDelegate.init(viewModelScope = this@GoodsListViewModel::viewModelScope,
                    scanResultHandler = this@GoodsListViewModel::handleProductSearchResult,
                    storePlace = storePlaceManager?.storePlaceNumber ?: "00")
        }
    }

    fun onResume() {
        if (justCreated) {
            val recountType = taskManager.getInventoryTask()?.taskDescription?.recountType
            if (recountType != RecountType.Simple) {
                makeLockUnlockRequest(recountType, StorePlaceLockMode.Lock, ::handleLockSuccess)
                justCreated = false
            }
        }
        updateUnprocessed()
        updateProcessed()
        processedSelectionHelper.clearPositions()
        unprocessedSelectionHelper.clearPositions()
        viewModelScope.launch {
            moveToPreviousPageIfNeeded()
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleLockSuccess(storePlaceLockInfo: StorePlaceLockRestInfo) {
        Logg.d { "tasksListRestInfo $storePlaceLockInfo" }
    }

    private fun handleUnlockSuccess(storePlaceLockInfo: StorePlaceLockRestInfo) {
        Logg.d { "tasksListRestInfo $storePlaceLockInfo" }
        screenNavigator.goBack()
    }

    fun updateProcessed() {
        val processed = storePlaceManager?.getProcessedProducts() ?: emptyList()
        processedGoods.postValue(processed.mapIndexed { index, productInfo ->
            ProductInfoVM(number = processed.size - index,
                    name = productInfo.getDisplayName(),
                    quantity = productInfo.getFormattedCount(),
                    matnr = productInfo.materialNumber)
        })
    }

    fun updateUnprocessed() {
        val unprocessed = storePlaceManager?.getNotProcessedProducts() ?: emptyList()
        unprocessedGoods.postValue(unprocessed.mapIndexed { index, productInfo ->
            ProductInfoVM(number = unprocessed.size - index,
                    name = productInfo.getDisplayName(),
                    quantity = productInfo.getFormattedCount(),
                    matnr = productInfo.materialNumber)
        })
    }

    fun onClickClean() {
        if (selectedPage.value == 0) {
            unprocessedSelectionHelper.selectedPositions.value?.forEach {
                unprocessedGoods.value?.get(it)?.matnr?.let {
                    taskManager.getInventoryTask()!!.deleteProduct(it)
                }
            }
            unprocessedSelectionHelper.clearPositions()
        } else {
            processedSelectionHelper.selectedPositions.value?.forEach {
                val matnr = processedGoods.value?.get(it)?.matnr
                if (matnr != null) {
                    val productInfo = taskManager.getInventoryTask()?.taskRepository?.getProducts()?.findProduct(matnr, storePlaceManager?.storePlaceNumber
                            ?: "")

                    if (productInfo != null) {
                        taskManager.getInventoryTask()!!.taskRepository.getProducts().changeProduct(productInfo.copy(factCount = 0.0, isPositionCalc = false))
                    }
                }
            }
            processedSelectionHelper.clearPositions()
        }
        updateUnprocessed()
        updateProcessed()
        viewModelScope.launch {
            moveToPreviousPageIfNeeded()
        }
    }

    fun onClickComplete() {

        val recountType = taskManager.getInventoryTask()?.taskDescription?.recountType
        if (recountType == RecountType.ParallelByStorePlaces) {
            storePlaceManager?.markAsProcessed()
            makeLockUnlockRequest(recountType, StorePlaceLockMode.Unlock, ::handleUnlockSuccess)
            return
        }

        taskManager.getInventoryTask()?.let {
            if (it.hasDiscrepancies()) {
                screenNavigator.openDiscrepanciesScreen()
            } else {
                screenNavigator.openConfirmationSavingJobScreen {
                    dataSaver.saveData(true)
                }
            }
        }
    }

    fun onClickBack() {
        val recountType = taskManager.getInventoryTask()?.taskDescription?.recountType
        if (recountType != RecountType.Simple) {
            makeLockUnlockRequest(recountType, StorePlaceLockMode.Unlock, ::handleUnlockSuccess)
        } else {
            screenNavigator.goBack()
        }
    }

    private fun makeLockUnlockRequest(recountType: RecountType?, mode: StorePlaceLockMode, successCallback: (StorePlaceLockRestInfo) -> Unit) {
        viewModelScope.launch {
            screenNavigator.showProgress(lockRequest)
            taskManager.getInventoryTask()?.let {
                val userNumber = if (recountType == RecountType.ParallelByPerNo) sessionInfo.personnelNumber
                        ?: "" else "" // указываем номер только при пересчете по номерам
                val storePlaceCode = if (recountType == RecountType.ParallelByStorePlaces) storePlaceManager?.storePlaceNumber
                        ?: "" else "" //указываем номер только при пересчете по МХ
                lockRequest(StorePlaceLockParams(ip = context.getDeviceIp(),
                        taskNumber = it.taskDescription.taskNumber,
                        storePlaceCode = storePlaceCode,
                        mode = mode.mode,
                        userNumber = userNumber)).either(::handleFailure, successCallback)
            }
            screenNavigator.hideProgress()
        }
    }

    fun onPageSelected(position: Int) {
        selectedPage.value = position
        lastPage = position
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToEan.value = true
        eanCode.value = eanCode.value ?: "" + digit
    }

    fun onScanResult(data: String) {
        searchProductDelegate.searchCode(code = data, fromScan = true)
    }

    private fun handleProductSearchResult(scanInfoResult: ScanInfoResult?): Boolean {
        eanCode.postValue("")
        return false
    }

    override fun onOkInSoftKeyboard(): Boolean {
        eanCode.value?.let {
            searchProductDelegate.searchCode(it, fromScan = false)
        }
        return true
    }

    fun onClickItemPosition(position: Int) {
        val matnr: String?
        if (selectedPage.value == 0) {
            matnr = unprocessedGoods.value?.get(position)?.matnr
        } else {
            matnr = processedGoods.value?.get(position)?.matnr
        }
        matnr?.let {
            val productInfo = taskManager.getInventoryTask()?.taskRepository?.getProducts()?.findProduct(it, storePlaceManager?.storePlaceNumber
                    ?: "")
            if (productInfo != null) searchProductDelegate.openTaskProductScreen(productInfo)
        }
    }

    fun onResult(code: Int?) {
        if (searchProductDelegate.handleResultCode(code)) {
            return
        }
    }

    private fun moveToPreviousPageIfNeeded() {
        if (lastPage == 0) {
            selectedPage.value = if (unprocessedGoods.value?.size == 0 && processedGoods.value?.size != 0) 1 else 0
        } else {
            selectedPage.value = if (processedGoods.value?.size == 0) 0 else 1
        }
    }
}

data class ProductInfoVM(
        val number: Int,
        val name: String,
        val quantity: String,
        val matnr: String
)