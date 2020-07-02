package com.lenta.movement.features.task.basket

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.movement.features.main.box.ScanInfoHelper
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.ProductInfo
import com.lenta.movement.models.SimpleListItem
import com.lenta.movement.models.repositories.ITaskBasketsRepository
import com.lenta.movement.platform.IFormatter
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.mapSkipNulls
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

class TaskBasketViewModel() : CoreViewModel(),
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

    var basketIndex by Delegates.notNull<Int>()

    val basket by lazy { taskBasketsRepository.getBasketByIndex(basketIndex) }

    val selectionsHelper = SelectionItemsHelper()

    val goods by lazy { MutableLiveData(getGoods()) }
    val goodsItemList by lazy {
        goods.mapSkipNulls { goods ->
            goods.mapIndexed { index, (product, count) ->
                SimpleListItem(
                        number = index + 1,
                        title = formatter.getProductName(product),
                        countWithUom = "$count $PCS_ABBREVIATION", // TODO
                        isClickable = false
                )
            }
        }
    }

    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val deleteEnabled = selectionsHelper.selectedPositions.map { selectedPositions ->
        selectedPositions.orEmpty().isNotEmpty()
    }

    fun getTitle(): String {
        return "${formatter.getBasketName(basket)}: ${formatter.getBasketDescription(
                basket,
                taskManager.getTask(),
                taskManager.getTaskSettings()
        )}"
    }

    fun onDeleteClick() {
        selectionsHelper.selectedPositions.value.orEmpty()
                .map { doRemoveProductIndex ->
                    taskBasketsRepository.getBasketByIndex(basketIndex)
                            .getByIndex(doRemoveProductIndex)
                }
                .forEach { doRemoveProduct ->
                    taskBasketsRepository.getBasketByIndex(basketIndex).remove(doRemoveProduct)
                }

        selectionsHelper.clearPositions()
        goods.postValue(getGoods())
    }

    fun onCharacteristicsClick() {
        screenNavigator.openTaskBasketCharacteristicsScreen(basketIndex)
    }

    fun onNextClick() {
        screenNavigator.goBack()
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

    private fun getGoods(): List<Pair<ProductInfo, Int>> {
        return taskBasketsRepository.getBasketByIndex(basketIndex).toList()
    }

    private fun searchCode(code: String, fromScan: Boolean, isBarCode: Boolean? = null) {
        viewModelScope.launch {
            scanInfoHelper.searchCode(code, fromScan, isBarCode) { productInfo ->
                // TODO
            }
        }
    }

    companion object {
        private const val PCS_ABBREVIATION = "шт."
    }
}