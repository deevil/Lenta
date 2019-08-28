package com.lenta.inventory.features.discrepancies_found

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.features.goods_list.DataSaver
import com.lenta.inventory.features.goods_list.SearchProductDelegate
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.StorePlaceLockMode
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.requests.network.StorePlaceLockNetRequest
import com.lenta.inventory.requests.network.StorePlaceLockParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import kotlinx.coroutines.launch
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class DiscrepanciesFoundViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IInventoryTaskManager
    @Inject
    lateinit var dataSaver: DataSaver
    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate
    @Inject
    lateinit var lockRequest: StorePlaceLockNetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var deviceInfo: DeviceInfo
    @Inject
    lateinit var timeMonitor: ITimeMonitor

    val discrepanciesByGoods: MutableLiveData<List<DiscrepancyVM>> = MutableLiveData()
    val discrepanciesByStorage: MutableLiveData<List<DiscrepancyVM>> = MutableLiveData()
    val processedGoods: MutableLiveData<List<TaskProductInfo>> = MutableLiveData()

    var selectedPage = MutableLiveData(0)

    val byGoodsSelectionHelper = SelectionItemsHelper()
    val byStorageSelectionHelper = SelectionItemsHelper()

    val untieDeleteEnabled: MutableLiveData<Boolean> = selectedPage
            .combineLatest(byGoodsSelectionHelper.selectedPositions.combineLatest(byStorageSelectionHelper.selectedPositions))
            .combineLatest(processedGoods.combineLatest(discrepanciesByGoods)).map {
                val page = it?.first?.first ?: 0
                val selectionCount = if (page == 0) it?.first?.second?.first?.size
                        ?: 0 else it?.first?.second?.second?.size ?: 0
                if (page == 0) {
                    val isStrict = taskManager.getInventoryTask()?.taskDescription?.isStrict == true
                    val allSelected = selectionCount == it?.second?.second?.size
                    val allUnprocessed = it?.second?.first?.size == 0
                    selectionCount > 0 && !isStrict && !(allSelected && allUnprocessed)
                } else {
                    selectionCount > 0
                }
            }

    val isNotEmpty: MutableLiveData<Boolean> = discrepanciesByGoods.map { it?.size != 0 }

    init {
        viewModelScope.launch {
            dataSaver.setViewModelScopeFunc(::viewModelScope)
            update()
        }
    }

    fun onResume() {
        update()
        byGoodsSelectionHelper.clearPositions()
        byStorageSelectionHelper.clearPositions()
    }

    fun update() {
        updateByGoods()
        updateByStorage()
        processedGoods.value = taskManager.getInventoryTask()?.taskRepository?.getProducts()?.getProcessedProducts()
    }

    fun updateByGoods() {
        val goods = taskManager.getInventoryTask()?.getDiscrepancies()?.distinctBy {
            it.materialNumber
        } ?: emptyList()

        val discrepancies = goods.mapIndexed { index, product ->
            DiscrepancyVM(number = goods.size - index,
                    name = product.getDisplayName(),
                    place = "",
                    matnr = product.materialNumber
            )
        }
        discrepanciesByGoods.postValue(discrepancies)
    }

    fun updateByStorage() {
        val goods = taskManager.getInventoryTask()?.getDiscrepancies()?.filter { it.placeCode != "00" }
                ?: emptyList()
        val discrepancies = goods.mapIndexed { index, product ->
            DiscrepancyVM(number = goods.size - index,
                    name = product.getDisplayName(),
                    place = product.placeCode,
                    matnr = product.materialNumber
            )
        }
        discrepanciesByStorage.postValue(discrepancies)
    }

    fun getTitle(): String {
        return taskManager.getInventoryTask()?.taskDescription?.getTaskTypeAndNumber() ?: ""
    }

    fun onClickMissing() {
        var count: Int
        if (selectedPage.value == 0) {
            count = byGoodsSelectionHelper.selectedPositions.value?.size ?: 0
            if (count == 0) {
                byGoodsSelectionHelper.addAll(discrepanciesByGoods.value ?: emptyList())
                count = byGoodsSelectionHelper.selectedPositions.value?.size ?: 0
            }
        } else {
            count = byStorageSelectionHelper.selectedPositions.value?.size ?: 0
            if (count == 0) {
                byStorageSelectionHelper.addAll(discrepanciesByStorage.value ?: emptyList())
                count = byStorageSelectionHelper.selectedPositions.value?.size ?: 0
            }
        }

        screenNavigator.openConfirmationMissingGoods(count) {
            if (selectedPage.value == 0) {
                byGoodsSelectionHelper.selectedPositions.value?.forEach {
                    discrepanciesByGoods.value?.get(it)?.let {
                        taskManager.getInventoryTask()?.markProductMissing(it.matnr)
                    }
                }
                byGoodsSelectionHelper.clearPositions()
            } else {
                byStorageSelectionHelper.selectedPositions.value?.forEach {
                    discrepanciesByStorage.value?.get(it)?.let {
                        taskManager.getInventoryTask()?.markProductMissing(it.matnr, it.place)
                    }
                }
                byStorageSelectionHelper.clearPositions()
            }
            update()
        }
    }

    fun onClickSkip() {
        if (isNotEmpty.value == true) {
            if (taskManager.getInventoryTask()!!.taskDescription.ivCountPerNr) {
                checkIsForkAnotherUsersNow()
            } else {
                screenNavigator.openConfirmationSkippingDiscrepancies(
                        elapsedTime = taskManager.getInventoryTask()?.getElapsedTimePrintable(timeMonitor.getUnixTime())
                                ?: "",
                        callbackFunc = {
                            dataSaver.saveData(false)
                        }
                )
            }

        } else {
            screenNavigator.openConfirmationSavingJobScreen {
                dataSaver.saveData(true)
            }
        }
    }

    private fun checkIsForkAnotherUsersNow() {

        viewModelScope.launch {
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
                ).either(::handleFailureUpdateStatus) {
                    openConfirmationSkippingDialog()
                }
            }
            screenNavigator.hideProgress()
        }


    }

    private fun handleFailureUpdateStatus(failure: Failure) {
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

    private fun openConfirmationSkippingDialog() {
        screenNavigator.openConfirmationSkippingDiscrepanciesRecount(
                elapsedTime = taskManager.getInventoryTask()?.getElapsedTimePrintable(timeMonitor.getUnixTime())
                        ?: "",
                rightCallbackFunc = {
                    dataSaver.saveData(true)
                },
                middleCallbackFunc = {
                    dataSaver.saveData(false)
                }
        )
    }

    fun onClickDeleteUntie() {
        if (selectedPage.value == 0) {
            byGoodsSelectionHelper.selectedPositions.value?.forEach {
                discrepanciesByGoods.value?.get(it)?.let {
                    taskManager.getInventoryTask()?.deleteProduct(it.matnr)
                }
            }
            byGoodsSelectionHelper.clearPositions()
        } else {
            byStorageSelectionHelper.selectedPositions.value?.forEach {
                discrepanciesByStorage.value?.get(it)?.let {
                    taskManager.getInventoryTask()?.untieProduct(it.matnr, it.place)
                }
            }
            byStorageSelectionHelper.clearPositions()
        }
        update()
    }

    fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun isRecountByStorePlaces(): Boolean {
        return taskManager.getInventoryTask()?.taskDescription?.recountType == RecountType.ParallelByStorePlaces
    }

    fun onClickItemPosition(position: Int) {
        val selectedDiscrepancy: DiscrepancyVM?
        if (selectedPage.value == 0) {
            selectedDiscrepancy = discrepanciesByGoods.value?.get(position)
        } else {
            selectedDiscrepancy = discrepanciesByStorage.value?.get(position)
        }

        if (!(selectedPage.value == 0 && isRecountByStorePlaces())) {
            selectedDiscrepancy?.let {
                val placeCode = if (selectedPage.value == 0) "00" else it.place
                val productInfo = taskManager.getInventoryTask()?.taskRepository?.getProducts()?.findProduct(it.matnr, placeCode)
                if (productInfo != null) searchProductDelegate.openTaskProductScreen(productInfo)
            }
        }
    }
}

data class DiscrepancyVM(
        val number: Int,
        val name: String,
        val place: String,
        val matnr: String
)
