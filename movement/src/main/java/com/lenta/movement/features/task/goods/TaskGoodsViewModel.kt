package com.lenta.movement.features.task.goods

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.movement.features.main.box.ScanInfoHelper
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.SimpleListItem
import com.lenta.movement.models.repositories.ITaskBasketsRepository
import com.lenta.movement.platform.IFormatter
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.mapSkipNulls
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskGoodsViewModel : CoreViewModel(),
    PageSelectionListener,
    OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: ITaskManager

    @Inject
    lateinit var taskBasketsRepository: ITaskBasketsRepository

    @Inject
    lateinit var scanInfoHelper: ScanInfoHelper

    @Inject
    lateinit var formatter: IFormatter

    val processedSelectionHelper = SelectionItemsHelper()
    val basketSelectionHelper = SelectionItemsHelper()

    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val processedList = MutableLiveData<List<SimpleListItem>>()
    val basketList = MutableLiveData<List<SimpleListItem>>()

    val selectedPagePosition = MutableLiveData(0)
    val currentPage = selectedPagePosition.mapSkipNulls { TaskGoodsPage.values()[it] }

    val deleteEnabled = combineLatest(
        currentPage,
        processedSelectionHelper.selectedPositions,
        basketSelectionHelper.selectedPositions
    )
        .mapSkipNulls { (currentPage, processedSelectedPositions, basketSelectedPositions) ->
            when (currentPage!!) {
                TaskGoodsPage.PROCESSED -> processedSelectedPositions.orEmpty().isNotEmpty()
                TaskGoodsPage.BASKETS -> basketSelectedPositions.orEmpty().isNotEmpty()
            }
        }

    fun onResume() {
        processedList.postValue(getProcessedSimpleList())
        basketList.postValue(getBasketSimpleList())
    }

    override fun onPageSelected(position: Int) {
        selectedPagePosition.value = position
    }

    fun getTitle(): String {
        return "${taskManager.getTask().taskType.shortName} // ${taskManager.getTask().name}"
    }

    override fun onOkInSoftKeyboard(): Boolean {
        searchCode(eanCode.value.orEmpty(), fromScan = false)

        return true
    }

    fun onScanResult(data: String) {
        searchCode(code = data, fromScan = true, isBarCode = true)
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToEan.value = true
        eanCode.value = eanCode.value ?: "" + digit
    }

    fun onBackPressed() {
        // TODO
        screenNavigator.goBack()
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

                processedList.postValue(getProcessedSimpleList())
                basketList.postValue(getBasketSimpleList())
            }
            TaskGoodsPage.BASKETS -> {
                taskBasketsRepository.getAll()
                    .filterIndexed { index, _ ->
                        basketSelectionHelper.selectedPositions.value.orEmpty().contains(index)
                    }
                    .forEach { doRemoveBasket ->
                        taskBasketsRepository.removeBasket(doRemoveBasket)
                    }

                processedList.postValue(getProcessedSimpleList())
                basketList.postValue(getBasketSimpleList())
            }
        }
    }

    fun onSaveClick() {
        // TODO
    }

    private fun searchCode(code: String, fromScan: Boolean, isBarCode: Boolean? = null) {
        viewModelScope.launch {
            scanInfoHelper.searchCode(code, fromScan, isBarCode) { productInfo ->
                screenNavigator.openTaskGoodsInfoScreen(productInfo)
            }
        }
    }

    private fun getProcessedSimpleList(): List<SimpleListItem> {
        return taskBasketsRepository.getAll()
            .flatMap { it.entries }
            .groupBy { (productInfo, _) -> productInfo }
            .mapValues { it.value.sumBy { it.value } }
            .toList()
            .mapIndexed { index, (productInfo, count) ->
                SimpleListItem(
                    number = index + 1,
                    title = formatter.getProductName(productInfo),
                    countWithUom = "$count ${Uom.DEFAULT.name}"
                )
            }
    }

    private fun getBasketSimpleList(): List<SimpleListItem> {
        return taskBasketsRepository.getAll()
            .map { basket ->
                SimpleListItem(
                    number = basket.number,
                    title = formatter.getBasketName(basket),
                    subtitle = formatter.getBasketDescription(basket, taskManager.getTask()),
                    countWithUom = basket.keys.size.toString()
                )
            }
    }
}