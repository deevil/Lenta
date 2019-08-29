package com.lenta.bp14.features.not_exposed.goods_list

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.data.GoodsListTab
import com.lenta.bp14.data.TaskManager
import com.lenta.bp14.data.model.Good
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
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

    private val selectedItemOnCurrentTab: MutableLiveData<Boolean> = selectedPage
            .combineLatest(processedSelectionsHelper.selectedPositions)
            .map {
                val tab = it?.first?.toInt()
                val processedSelected = it?.second?.isNotEmpty() == true
                tab == GoodsListTab.PROCESSED.position && processedSelected || tab == GoodsListTab.SEARCH.position
            }

    val deleteButtonEnabled = selectedItemOnCurrentTab.map { it }
    val saveButtonEnabled = processingGoods.map { it?.isNotEmpty() ?: false }

    val thirdButtonVisibility = selectedPage.map { it != GoodsListTab.PROCESSING.position }

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

    override fun onOkInSoftKeyboard(): Boolean {
        return false
    }

    fun scanQrCode() {

    }

    fun scanBarCode() {

    }

    fun onClickSave() {

    }

    fun onClickDelete() {

    }

    fun onClickFilter() {
        navigator.openSearchFilterWlScreen()
    }

    fun onClickItemPosition(position: Int) {
        taskManager.currentGood = getGoodByPosition(position)
        navigator.openGoodInfoPcScreen()
    }

    private fun getGoodByPosition(position: Int): Good? {
        return when (selectedPage.value) {
            GoodsListTab.PROCESSING.position -> processingGoods.value?.get(position)
            GoodsListTab.PROCESSED.position -> processedGoods.value?.get(position)
            GoodsListTab.SEARCH.position -> searchGoods.value?.get(position)
            else -> null
        }
    }

}