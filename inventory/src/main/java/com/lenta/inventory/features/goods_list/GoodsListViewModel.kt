package com.lenta.inventory.features.goods_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.R
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.StorePlaceLockMode
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.models.task.StorePlaceProcessing
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.requests.network.StorePlaceLockNetRequest
import com.lenta.inventory.requests.network.StorePlaceLockParams
import com.lenta.inventory.requests.network.StorePlaceLockRestInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
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
    @Inject
    lateinit var deviceInfo: DeviceInfo

    val unprocessedGoods: MutableLiveData<List<ProductInfoVM>> = MutableLiveData()
    val processedGoods: MutableLiveData<List<ProductInfoVM>> = MutableLiveData()
    var selectedPage = MutableLiveData(0)

    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val onCompleteButtonEnabled: MutableLiveData<Boolean> = processedGoods.map {
        it?.isNotEmpty() ?: false
    }

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
        launchUITryCatch {
            dataSaver.setViewModelScopeFunc(::viewModelScope)
        }

    }

    fun getTitle(): String {
        return when (taskManager.getInventoryTask()?.taskDescription?.recountType) {
            RecountType.Simple -> "${taskManager.getInventoryTask()?.taskDescription?.getTaskTypeAndNumber()
                   .orEmpty()} / ${context.getString(R.string.simple_recount)}"
            RecountType.ParallelByStorePlaces -> "${taskManager.getInventoryTask()?.taskDescription?.getTaskTypeAndNumber()
                   .orEmpty()} / МХ-${storePlaceManager?.storePlaceNumber}"
            RecountType.ParallelByPerNo -> "${taskManager.getInventoryTask()?.taskDescription?.getTaskTypeAndNumber()
                   .orEmpty()} / ${sessionInfo.personnelFullName}"
            else -> taskManager.getInventoryTask()?.taskDescription?.getTaskTypeAndNumber().orEmpty()
        }
    }

    fun setStorePlaceNumber(storePlaceNumber: String) {
        storePlaceManager = taskManager.getInventoryTask()!!.processStorePlace(storePlaceNumber = storePlaceNumber)
    }

    init {
        launchUITryCatch {
            searchProductDelegate.init(viewModelScope = this@GoodsListViewModel::viewModelScope,
                    scanResultHandler = this@GoodsListViewModel::handleProductSearchResult,
                    storePlace = storePlaceManager?.storePlaceNumber ?: "00")
            if (taskManager.getInventoryTask()?.taskDescription?.recountType == RecountType.ParallelByStorePlaces) {
                taskManager.getInventoryTask()?.makeSnapshot()
            }
        }
    }

    fun onResume() {
        if (justCreated) {
            val recountType = taskManager.getInventoryTask()?.taskDescription?.recountType
            if (recountType == RecountType.ParallelByStorePlaces && storePlaceManager!!.getProcessedProducts().isEmpty()) {
                makeLockUnlockRequest(recountType, StorePlaceLockMode.Lock, ::handleLockSuccess)
                justCreated = false
            }
        }
        updateUnprocessed()
        updateProcessed()
        processedSelectionHelper.clearPositions()
        unprocessedSelectionHelper.clearPositions()
        launchUITryCatch {
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
        storePlaceManager!!.markAsNotProcessed()
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
                unprocessedGoods.value?.get(it)?.matnr?.let { matNr ->
                    taskManager.getInventoryTask()!!.deleteProduct(matNr)
                }
            }
            unprocessedSelectionHelper.clearPositions()
        } else {
            processedSelectionHelper.selectedPositions.value?.forEach {
                val matnr = processedGoods.value?.get(it)?.matnr
                if (matnr != null) {
                    val productInfo = taskManager.getInventoryTask()?.taskRepository?.getProducts()?.findProduct(matnr, storePlaceManager?.storePlaceNumber
                           .orEmpty())

                    if (productInfo != null) {
                        taskManager.getInventoryTask()!!.taskRepository.apply {
                            getExciseStamps().deleteExciseStampsForProduct(productInfo)
                            getProducts().changeProduct(productInfo.copy(factCount = 0.0, isPositionCalc = false))
                        }
                    }
                }
            }
            processedSelectionHelper.clearPositions()
        }
        updateUnprocessed()
        updateProcessed()
        launchUITryCatch {
            moveToPreviousPageIfNeeded()
        }
    }

    fun onClickComplete() {
        val recountType = taskManager.getInventoryTask()?.taskDescription?.recountType
        if (recountType == RecountType.ParallelByStorePlaces) {
            storePlaceManager?.markAsProcessed()
            if (storePlaceManager!!.getProcessedProducts().isEmpty()) {
                makeLockUnlockRequest(recountType, StorePlaceLockMode.Unlock, ::handleUnlockSuccess)
            } else {
                screenNavigator.goBack()
            }
            return
        }

        taskManager.getInventoryTask()?.let {
            if (it.hasDiscrepancies()) {
                screenNavigator.openDiscrepanciesScreen()
            } else {
                if (taskManager.getInventoryTask()!!.taskDescription.ivCountPerNr) {
                    checkIsHaveAnotherUsersNow()
                } else {
                    screenNavigator.openConfirmationSavingJobScreen {
                        dataSaver.saveData(true)
                    }
                }

            }
        }
    }

    fun onClickBack() {
        val recountType = taskManager.getInventoryTask()?.taskDescription?.recountType

        if (recountType == RecountType.ParallelByStorePlaces) {
            if (taskManager.getInventoryTask()?.isChanged() == true) {
                screenNavigator.openConfirmationExitStoreplace {
                    taskManager.getInventoryTask()?.restoreSnapshot()
                    unlockIfNeededAndGoBack(needsUnlock = storePlaceManager!!.getProcessedProducts().isEmpty())
                }
            } else {
                unlockIfNeededAndGoBack(needsUnlock = storePlaceManager!!.getProcessedProducts().isEmpty())
            }
        } else {
            screenNavigator.goBack()
        }
    }

    private fun unlockIfNeededAndGoBack(needsUnlock: Boolean) {
        if (needsUnlock) {
            makeLockUnlockRequest(RecountType.ParallelByStorePlaces, StorePlaceLockMode.Unlock, ::handleUnlockSuccess)
        } else {
            screenNavigator.goBack()
        }
    }

    private fun checkIsHaveAnotherUsersNow() {
        launchUITryCatch {
            screenNavigator.showProgress(lockRequest)
            taskManager.getInventoryTask()?.let {
                val userNumber = sessionInfo.personnelNumber!!
                lockRequest(
                        StorePlaceLockParams(
                                ip = deviceInfo.getDeviceIp(),
                                taskNumber = it.taskDescription.taskNumber,
                                storePlaceCode = "",
                                mode = StorePlaceLockMode.Check.mode,
                                userNumber = userNumber
                        )
                ).either(::handleFailureCheckAnotherUserStatus) {
                    screenNavigator.openConfirmationSavingJobScreen {
                        dataSaver.saveData(true)
                    }
                }
            }
            screenNavigator.hideProgress()
        }


    }

    private fun handleFailureCheckAnotherUserStatus(failure: Failure) {
        super.handleFailure(failure)

        if (failure is Failure.SapError) {
            if (failure.retCode == 2) {
                openConfirmationWithAnotherParallelsActiveUserDialog()
                return
            }
        }
        screenNavigator.openAlertScreen(failure)

    }

    private fun openConfirmationWithAnotherParallelsActiveUserDialog() {
        screenNavigator.openConfirmationSavingForParallelsActiveUserDialog {
            dataSaver.saveData(false)
        }
    }


    private fun makeLockUnlockRequest(recountType: RecountType?, mode: StorePlaceLockMode, successCallback: (StorePlaceLockRestInfo) -> Unit) {
        launchUITryCatch {
            screenNavigator.showProgress(lockRequest)
            taskManager.getInventoryTask()?.let {
                val userNumber = if (recountType == RecountType.ParallelByPerNo) sessionInfo.personnelNumber
                       .orEmpty() else "" // указываем номер только при пересчете по номерам
                val storePlaceCode = if (recountType == RecountType.ParallelByStorePlaces) storePlaceManager?.storePlaceNumber
                       .orEmpty() else "" //указываем номер только при пересчете по МХ
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
        eanCode.value = eanCode.value.orEmpty() + digit
    }

    fun onScanResult(data: String) {
        searchProductDelegate.searchCode(code = data, fromScan = true, isBarCode = true)
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

    fun onClickItemPosition(position: Int) {
        val matnr: String?
        if (selectedPage.value == 0) {
            matnr = unprocessedGoods.value?.get(position)?.matnr
        } else {
            matnr = processedGoods.value?.get(position)?.matnr
        }
        matnr?.let {
            val productInfo = taskManager.getInventoryTask()?.taskRepository?.getProducts()?.findProduct(it, storePlaceManager?.storePlaceNumber
                   .orEmpty())
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