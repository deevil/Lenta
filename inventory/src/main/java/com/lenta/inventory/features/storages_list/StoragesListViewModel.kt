package com.lenta.inventory.features.storages_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.R
import com.lenta.inventory.features.goods_list.DataSaver
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.StorePlaceLockMode
import com.lenta.inventory.models.StorePlaceStatus
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.models.task.TaskContents
import com.lenta.inventory.models.task.TaskStorePlaceInfo
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.requests.network.StorePlaceLockNetRequest
import com.lenta.inventory.requests.network.StorePlaceLockParams
import com.lenta.inventory.requests.network.TaskContentNetRequest
import com.lenta.inventory.requests.network.TaskContentParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class StoragesListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var taskManager: IInventoryTaskManager
    @Inject
    lateinit var taskContentsRequest: TaskContentNetRequest
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var dataSaver: DataSaver
    @Inject
    lateinit var lockRequest: StorePlaceLockNetRequest

    val unprocessedStorages: MutableLiveData<List<StoragePlaceVM>> = MutableLiveData()
    val processedStorages: MutableLiveData<List<StoragePlaceVM>> = MutableLiveData()
    var selectedPage = MutableLiveData(0)

    val completeEnabled: MutableLiveData<Boolean> = processedStorages.map { it?.isNotEmpty() == true }

    val storageNumber: MutableLiveData<String> = MutableLiveData()
    val requestFocusToStorageNumber: MutableLiveData<Boolean> = MutableLiveData()
    val processedSelectionHelper = SelectionItemsHelper()

    var lastPage: Int = 0

    val deleteEnabled: MutableLiveData<Boolean> = selectedPage.combineLatest(processedSelectionHelper.selectedPositions).map {
        val page = it?.first ?: 0
        val selectionCount = it?.second?.size ?: 0
        page != 0 && selectionCount > 0
    }

    private var needsUpdate: Boolean = false

    init {
        launchUITryCatch {
            dataSaver.setViewModelScopeFunc(::viewModelScope)
            updateUnprocessed()
            updateProcessed()
        }
    }

    fun onResume() {
        Logg.d { "needsUpdate: $needsUpdate" }
        if (needsUpdate) {
            onClickRefresh()
            needsUpdate = false
        } else {
            updateUnprocessed()
            updateProcessed()
        }
    }

    fun getTitle(): String {
        return taskManager.getInventoryTask()?.taskDescription?.getTaskTypeAndNumber().orEmpty()
    }

    private fun updateProcessed() {
        taskManager.getInventoryTask()?.let {
            val processed = it.getProcessedStorePlaces().mapIndexed { index, storePlace ->
                val productsQuantity = it.getProductsQuantityForStorePlace(storePlace.placeCode)
                StoragePlaceVM.from(storePlace, productsQuantity, index + 1)
            }
            processedStorages.postValue(processed)
            return
        }
        processedStorages.postValue(emptyList())
    }

    private fun updateUnprocessed() {
        taskManager.getInventoryTask()?.let {
            val unprocessed = it.getUnprocessedStorePlaces().mapIndexed { index, storePlace ->
                val productsQuantity = it.getProductsQuantityForStorePlace(storePlace.placeCode)
                StoragePlaceVM.from(storePlace, productsQuantity, index + 1)
            }
            unprocessedStorages.postValue(unprocessed)
            return
        }
        unprocessedStorages.postValue(emptyList())
    }

    fun onClickClean() {
        screenNavigator.openConfirmationClean(byStorage = true) {
            launchUITryCatch {
                screenNavigator.showProgress(lockRequest)
                Logg.d { "processedSelectionHelper.selectedPositions size: ${processedSelectionHelper.selectedPositions.value?.size}" }
                val selectedPositions = processedSelectionHelper.selectedPositions.value
                        ?: emptySet<Int>()
                for (position in selectedPositions) {
                    processedStorages.value?.get(position)?.let { storePlaceVm ->
                        taskManager.getInventoryTask()?.let {
                            lockRequest(StorePlaceLockParams(ip = context.getDeviceIp(),
                                    taskNumber = it.taskDescription.taskNumber,
                                    storePlaceCode = storePlaceVm.storeNumber,
                                    mode = StorePlaceLockMode.Unlock.mode,
                                    userNumber = "")).either(::handleFailure) {
                                taskManager.getInventoryTask()?.clearStorePlaceByNumber(storePlaceVm.storeNumber)
                                return@either false
                            }
                        }

                    }
                }
                processedSelectionHelper.clearPositions()
                screenNavigator.hideProgress()
                onClickRefresh()
                moveToPreviousPageIfNeeded()
            }
        }
    }

    fun onClickComplete() {

        taskManager.getInventoryTask()?.let {
            needsUpdate = false
            Logg.d { "needsUpdate set $needsUpdate" }
            if (it.hasDiscrepancies()) {
                screenNavigator.openDiscrepanciesScreen()
            } else {
                screenNavigator.openConfirmationSavingJobScreen {
                    dataSaver.saveData(true)
                }
            }
        }
    }

    fun onClickRefresh() {
        launchUITryCatch {
            screenNavigator.showProgress(taskContentsRequest)
            val recountType = taskManager.getInventoryTask()?.taskDescription?.recountType
            val isNotFinish = taskManager.getInventoryTask()?.taskDescription?.isStarted
            val userNumber = if (recountType == RecountType.ParallelByStorePlaces || sessionInfo.personnelNumber == null || isNotFinish == true) "" else sessionInfo.personnelNumber
            taskContentsRequest(
                    TaskContentParams(ip = context.getDeviceIp(),
                            taskNumber = taskManager.getInventoryTask()?.taskDescription?.taskNumber
                                   .orEmpty(),
                            userNumber = userNumber.orEmpty(),
                            additionalDataFlag = "",
                            newProductNumbers = emptyList(),
                            numberRelock = "",
                            mode = "3")
            )
                    .either(::handleFailure, ::handleUpdateSuccess)
            screenNavigator.hideProgress()
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleUpdateSuccess(taskContents: TaskContents) {
        taskManager.getInventoryTask()?.let {
            it.updateTaskWithContents(taskContents)
            updateProcessed()
            updateUnprocessed()
            launchUITryCatch {
                moveToPreviousPageIfNeeded()
            }
        }
    }

    fun onPageSelected(position: Int) {
        selectedPage.value = position
        lastPage = position
    }

    fun onClickItemPosition(position: Int) {
        val storeNumber: String?
        if (selectedPage.value == 0) {
            storeNumber = unprocessedStorages.value?.get(position)?.storeNumber
        } else {
            storeNumber = processedStorages.value?.get(position)?.storeNumber
        }
        storeNumber?.let { storePlaceNumber ->
            val storePlace = taskManager.getInventoryTask()!!.taskRepository.getStorePlace().findStorePlace(storePlaceNumber)
            needsUpdate = true
            when (storePlace?.status) {
                StorePlaceStatus.LockedByMe, StorePlaceStatus.LockedByOthers ->
                    screenNavigator.openConfirmationTakeStorePlace {
                        screenNavigator.openGoodsListScreen(storePlaceNumber)
                    }
                StorePlaceStatus.None, StorePlaceStatus.Started ->
                    screenNavigator.openGoodsListScreen(storePlaceNumber)
                StorePlaceStatus.Finished ->
                    screenNavigator.openAlertScreen(context.getString(R.string.already_counted))
            }
        }
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToStorageNumber.value = true
        storageNumber.value = storageNumber.value.orEmpty() + digit
    }

    fun onScanResult(data: String) {
        storageNumber.value = data
    }

    override fun onOkInSoftKeyboard(): Boolean {
        if (!storageNumber.value.isNullOrEmpty()) {
            val storageNumber = storageNumber.value!!
            taskManager.getInventoryTask()?.let {
                val existingPlace = it.taskRepository.getStorePlace().findStorePlace(storageNumber)
                if (existingPlace == null) {
                    it.taskRepository.getStorePlace().addStorePlace(TaskStorePlaceInfo(placeCode = storageNumber, lockIP = "", lockUser = "", status = StorePlaceStatus.None, addedManually = true))
                    updateUnprocessed()
                }
                Logg.d { "needsUpdate set $needsUpdate" }
                needsUpdate = true
                screenNavigator.openLoadingStorePlaceLockScreen(StorePlaceLockMode.Lock, storageNumber)
            }
        }

        return true
    }

    private fun moveToPreviousPageIfNeeded() {
        if (lastPage == 0) {
            selectedPage.value = if (unprocessedStorages.value?.size == 0 && processedStorages.value?.size != 0) 1 else 0
        } else {
            selectedPage.value = if (processedStorages.value?.size == 0) 0 else 1
        }
    }
}

data class StoragePlaceVM(
        val number: Int,
        val storeNumber: String,
        val status: StorePlaceStatus,
        val name: String,
        val productsQuantity: Int,
        val selectable: Boolean) {

    companion object {
        fun from(taskStoragePlaceInfo: TaskStorePlaceInfo, productsQuantity: Int, number: Int): StoragePlaceVM {
            return StoragePlaceVM(
                    number = number,
                    storeNumber = taskStoragePlaceInfo.placeCode,
                    status = taskStoragePlaceInfo.status,
                    name = taskStoragePlaceInfo.placeCode,
                    productsQuantity = productsQuantity,
                    selectable = taskStoragePlaceInfo.status != StorePlaceStatus.Finished)
        }

    }
}
