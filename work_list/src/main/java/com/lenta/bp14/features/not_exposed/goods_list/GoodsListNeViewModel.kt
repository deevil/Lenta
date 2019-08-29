package com.lenta.bp14.features.not_exposed.goods_list

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.data.PriceCheckTab
import com.lenta.bp14.data.TaskManager
import com.lenta.bp14.data.model.Good
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListNeViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: TaskManager


    val processedSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val taskName = MutableLiveData("Невыставленный товар от 23.07.19 23:15")

    val numberField: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToNumberField: MutableLiveData<Boolean> = MutableLiveData()

    val processingGoods = MutableLiveData<List<Good>>()
    val processedGoods = MutableLiveData<List<Good>>()
    val searchGoods = MutableLiveData<List<Good>>()

    init {
        viewModelScope.launch {
            requestFocusToNumberField.value = true

            // Тестовые данные
            processingGoods.value = taskManager.getTestGoodList(3)
            processedGoods.value = taskManager.getTestGoodList(28)
            searchGoods.value = taskManager.getTestGoodList(2)
        }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickSave() {

    }

    override fun onOkInSoftKeyboard(): Boolean {
        return false
    }

    fun scanQrCode() {

    }

    fun scanBarCode() {

    }

    fun onClickItemPosition(position: Int) {
        taskManager.currentGood = getGoodByPosition(position)
        navigator.openGoodInfoPcScreen()
    }

    private fun getGoodByPosition(position: Int): Good? {
        return when (selectedPage.value) {
            PriceCheckTab.PROCESSING.position -> processingGoods.value?.get(position)
            PriceCheckTab.PROCESSED.position -> processedGoods.value?.get(position)
            PriceCheckTab.SEARCH.position -> searchGoods.value?.get(position)
            else -> null
        }
    }

}
