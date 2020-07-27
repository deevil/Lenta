package com.lenta.movement.features.task.goods

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.lenta.movement.features.main.box.ScanInfoHelper
import com.lenta.movement.models.*
import com.lenta.movement.models.repositories.ITaskBasketsRepository
import com.lenta.movement.platform.IFormatter
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.requests.network.SaveTaskNetRequest
import com.lenta.movement.requests.network.models.saveTask.SaveTaskParams
import com.lenta.movement.requests.network.models.saveTask.SaveTaskParamsTaskBasket
import com.lenta.movement.requests.network.models.saveTask.SaveTaskParamsTaskMaterial
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.*
import javax.inject.Inject

class TaskGoodsViewModel : CoreViewModel(),
        PageSelectionListener,
        OnOkInSoftKeyboardListener {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var taskManager: ITaskManager

    @Inject
    lateinit var taskBasketsRepository: ITaskBasketsRepository

    @Inject
    lateinit var scanInfoHelper: ScanInfoHelper

    @Inject
    lateinit var saveTaskNetRequest: SaveTaskNetRequest

    @Inject
    lateinit var formatter: IFormatter

    val processedSelectionHelper = SelectionItemsHelper()
    val basketSelectionHelper = SelectionItemsHelper()

    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    private val processed = MutableLiveData<List<Pair<ProductInfo, Int>>>()
    val processedList = processed.mapSkipNulls { processed ->
        processed.mapIndexed { index, (productInfo, count) ->
            SimpleListItem(
                    number = index + 1,
                    title = formatter.getProductName(productInfo),
                    countWithUom = "$count ${Uom.DEFAULT.name}",
                    isClickable = true
            )
        }
    }

    private val baskets = MutableLiveData<List<Basket>>()
    val basketItemList by unsafeLazy {
        baskets.switchMap { list ->
            asyncLiveData<List<SimpleListItem>> {
                val mappedList = list.map { basket ->
                    SimpleListItem(
                            number = basket.number,
                            title = formatter.getBasketName(basket),
                            subtitle = formatter.getBasketDescription(
                                    basket,
                                    taskManager.getTask(),
                                    taskManager.getTaskSettings()
                            ),
                            countWithUom = basket.keys.size.toString(),
                            isClickable = true
                    )
                }
                emit(mappedList)
            }
        }
    }

    val selectedPagePosition = MutableLiveData(0)
    val currentPage = selectedPagePosition.mapSkipNulls { TaskGoodsPage.values().getOrNull(it) }

    val deleteEnabled = combineLatest(
            currentPage,
            processedSelectionHelper.selectedPositions,
            basketSelectionHelper.selectedPositions
    )
            .mapSkipNulls { (currentPage, processedSelectedPositions, basketSelectedPositions) ->
                when (currentPage) {
                    TaskGoodsPage.PROCESSED -> processedSelectedPositions.orEmpty().isNotEmpty()
                    TaskGoodsPage.BASKETS -> basketSelectedPositions.orEmpty().isNotEmpty()
                    null -> false
                }
            }

    val saveEnabled = processed.mapSkipNulls {
        it.isNotEmpty()
    }

    fun onResume() {
        processed.postValue(getProcessed())
        baskets.postValue(getBaskets())
    }

    override fun onPageSelected(position: Int) {
        selectedPagePosition.value = position
    }

    fun getTitle(): String {
        return "${taskManager.getTask().taskType.shortName} // ${taskManager.getTask().name}"
    }

    override fun handleFragmentResult(code: Int?): Boolean {
        return scanInfoHelper.handleFragmentResult(code) || super.handleFragmentResult(code)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        searchCode(eanCode.value.orEmpty(), fromScan = false)
        return true
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToEan.value = true
        eanCode.value = eanCode.value ?: "" + digit
    }

    fun onBackPressed() {
        if (taskBasketsRepository.getAll().isNotEmpty()) {
            screenNavigator.openUnsavedDataDialog {
                taskBasketsRepository.clear()
                screenNavigator.goBack()
            }
        } else {
            screenNavigator.goBack()
        }
    }

    fun onClickProcessedItem(position: Int) {
        processed.value.orEmpty().getOrNull(position)?.also { (product, _) ->
            screenNavigator.openTaskGoodsInfoScreen(product)
        }
    }

    fun onClickBasketItem(position: Int) {
        baskets.value.orEmpty().getOrNull(position)?.also { basket ->
            screenNavigator.openTaskBasketScreen(basket.index)
        }
    }

    fun onDeleteClick() {
        when (currentPage.value) {
            TaskGoodsPage.PROCESSED -> {
                taskBasketsRepository.getAll()
                        .flatMap { it.keys }
                        .filterIndexed { index, _ ->
                            processedSelectionHelper.selectedPositions.value.orEmpty().contains(index)
                        }
                        .forEach { doRemoveProduct ->
                            taskBasketsRepository.removeProductFromAllBaskets(doRemoveProduct)
                        }

                processed.postValue(getProcessed())
                baskets.postValue(getBaskets())
                processedSelectionHelper.clearPositions()
            }
            TaskGoodsPage.BASKETS -> {
                taskBasketsRepository.getAll()
                        .filterIndexed { index, _ ->
                            basketSelectionHelper.selectedPositions.value.orEmpty().contains(index)
                        }
                        .forEach { doRemoveBasket ->
                            taskBasketsRepository.removeBasket(doRemoveBasket)
                        }

                processed.postValue(getProcessed())
                baskets.postValue(getBaskets())
                basketSelectionHelper.clearPositions()
            }
        }
    }

    fun onSaveClick() {
        screenNavigator.openSaveTaskConfirmationDialog(
                yesCallbackFunc = ::saveTask,
                status = Task.Status.COUNTED
        )
    }

    private fun saveTask() {
        launchUITryCatch {
            screenNavigator.showProgress(saveTaskNetRequest)
            val task = taskManager.getTask()
            val params = SaveTaskParams(
                    userNumber = sessionInfo.personnelNumber.orEmpty(),
                    deviceIp = context.getDeviceIp(),
                    tkNumber = sessionInfo.market.orEmpty(),
                    taskNumber = task.number,
                    taskName = task.name,
                    taskType = task.taskType,
                    movementType = task.movementType,
                    lgortSource = task.pikingStorage,
                    lgortTarget = task.shipmentStorage,
                    shipmentDate = task.shipmentDate.getFormattedDate(Constants.DATE_FORMAT_yyyy_mm_dd),
                    isNotFinish = task.isCreated.toSapBooleanString(),
                    destination = task.receiver,
                    materials = getProcessed().map { (product, count) ->
                        SaveTaskParamsTaskMaterial(
                                number = product.materialNumber,
                                quantity = count.toString(),
                                positionCounted = true.toSapBooleanString(),
                                isDeleted = false.toSapBooleanString(),
                                uom = Uom.ST.code
                        )
                    },
                    baskets = getBaskets().flatMap { basket ->
                        basket.map { (product, count) ->
                            SaveTaskParamsTaskBasket(
                                    basketNumber = basket.number.toString(),
                                    materialNumber = product.materialNumber,
                                    quantity = count.toString(),
                                    uom = Uom.ST.code,  // TODO Базисная единица измерения
                                    materialType = "",
                                    lifNr = basket.supplier?.code.orEmpty(),
                                    zcharg = "", // TODO Номер партии
                                    isAlco = basket.isAlco.toSapBooleanString(),
                                    isExcise = basket.isExciseAlco.toSapBooleanString(),
                                    isNotExcise = basket.isNotExciseAlco.toSapBooleanString(),
                                    isUsual = basket.isUsual.toSapBooleanString(),
                                    isVet = basket.isVet.toSapBooleanString(),
                                    isFood = basket.isFood.toSapBooleanString()
                            )
                        }
                    }
            )
            saveTaskNetRequest(params).either(
                    fnL = { failure ->
                        screenNavigator.openAlertScreen(failure)
                    },
                    fnR = { savedTask ->
                        taskBasketsRepository.clear()
                        screenNavigator.goBack()
                        screenNavigator.goBack()
                        taskManager.setTask(savedTask)
                        screenNavigator.openTaskScreen(savedTask)
                    }
            )

            screenNavigator.hideProgress()
        }
    }

    fun onScanResult(data: String) {
        searchCode(code = data, fromScan = true, isBarCode = true)
    }

    private fun searchCode(code: String, fromScan: Boolean, isBarCode: Boolean? = null) {
        launchUITryCatch {
            scanInfoHelper.searchCode(code, fromScan, isBarCode) { productInfo ->
                screenNavigator.openTaskGoodsInfoScreen(productInfo)
            }
        }
    }

    private fun getProcessed(): List<Pair<ProductInfo, Int>> {
        return taskBasketsRepository.getAll()
                .flatMap { it.entries }
                .groupBy { (productInfo, _) -> productInfo }
                .mapValues { it.value.sumBy { it.value } }
                .toList()
    }

    private fun getBaskets(): List<Basket> {
        return taskBasketsRepository.getAll()
    }
}