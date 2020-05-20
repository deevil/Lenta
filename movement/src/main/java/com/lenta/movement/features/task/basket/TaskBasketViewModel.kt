package com.lenta.movement.features.task.basket

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.movement.features.main.box.GoodListItem
import com.lenta.movement.features.main.box.ScanInfoHelper
import com.lenta.movement.models.repositories.ITaskBasketsRepository
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskBasketViewModel : CoreViewModel(),
    OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskBasketsRepository: ITaskBasketsRepository

    @Inject
    lateinit var scanInfoHelper: ScanInfoHelper

    var basketIndex: Int? = null

    val selectionsHelper = SelectionItemsHelper()

    val goodsItemList by lazy { MutableLiveData(getGoodList()) }

    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val deleteEnabled = selectionsHelper.selectedPositions.map { selectedPositions ->
        selectedPositions.orEmpty().isNotEmpty()
    }

    fun getTitle(): String {
        return "Корзина"
    }

    fun onDeleteClick() {
        selectionsHelper.selectedPositions.value.orEmpty()
            .map { doRemoveProductIndex ->
                taskBasketsRepository.getBasketByIndex(basketIndex!!).getByIndex(doRemoveProductIndex)
            }
            .forEach { doRemoveProduct ->
                taskBasketsRepository.getBasketByIndex(basketIndex!!).remove(doRemoveProduct)
            }

        selectionsHelper.clearPositions()

        goodsItemList.postValue(getGoodList())
    }

    fun onCharacteristicsClick() {
        screenNavigator.openTaskBasketCharacteristicsScreen(basketIndex!!)
    }

    fun onNextClick() {
        screenNavigator.goBack()
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

    private fun getGoodList(): List<GoodListItem> {
        return taskBasketsRepository.getBasketByIndex(basketIndex!!).toList()
            .mapIndexed { index, (product, count) ->
                GoodListItem(
                    number = index + 1,
                    name = "${product.getMaterialLastSix()} ${product.description}",
                    countWithUom = "$count шт."
                )
            }
    }

    private fun searchCode(code: String, fromScan: Boolean, isBarCode: Boolean? = null) {
        viewModelScope.launch {
            scanInfoHelper.searchCode(code, fromScan, isBarCode) { productInfo ->
                // TODO
            }
        }
    }

}