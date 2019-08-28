package com.lenta.bp14.features.price_check.goods_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.data.PriceCheckTab
import com.lenta.bp14.data.model.Good
import com.lenta.bp14.data.TaskManager
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListPcViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: TaskManager


    val processedSelectionsHelper = SelectionItemsHelper()
    val searchSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val taskName = MutableLiveData("Сверка цен на полке от 23.07.19 23:15")

    val processingGoods = MutableLiveData<List<Good>>(getTestItems())
    val processedGoods = MutableLiveData<List<Good>>(getTestItems())
    val searchGoods = MutableLiveData<List<Good>>(getTestItems())

    private val selectedItemOnCurrentTab: MutableLiveData<Boolean> = selectedPage
            .combineLatest(processedSelectionsHelper.selectedPositions)
            .combineLatest(searchSelectionsHelper.selectedPositions)
            .map {
                val tab = it?.first?.first?.toInt()
                val processedSelected = it?.first?.second?.isNotEmpty() == true
                val searchSelected = it?.second?.isNotEmpty() == true
                tab == PriceCheckTab.PROCESSED.position && processedSelected || tab == PriceCheckTab.SEARCH.position && searchSelected
    }

    val deleteButtonEnabled = selectedItemOnCurrentTab.map { it }
    val printButtonEnabled = selectedItemOnCurrentTab.map { it }

    val deleteButtonVisibility = selectedPage.map { it != PriceCheckTab.PROCESSING.position }
    val printButtonVisibility = selectedPage.map { it != PriceCheckTab.PROCESSING.position }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    init {
        viewModelScope.launch {
            selectedPage.value = 0
        }
    }

    private fun getTestItems(): List<Good>? {
        return List(3) {
            Good(
                    id = it + 1,
                    material = "000000000000" + (111111..999999).random(),
                    name = "Товар ${it + (1..99).random()}",
                    uom = Uom.DEFAULT
            )
        }
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

    fun onClickPrint() {

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