package com.lenta.bp14.features.price_check.goods_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.check_price.CheckPriceTaskManager
import com.lenta.bp14.models.data.GoodsListTab
import com.lenta.bp14.models.data.pojo.Good
import com.lenta.bp14.models.getTaskName
import com.lenta.bp14.models.getTaskType
import com.lenta.bp14.platform.navigation.IScreenNavigator
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
    lateinit var checkPriceTaskManager: CheckPriceTaskManager


    val processedSelectionsHelper = SelectionItemsHelper()
    val searchSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val correctedSelectedPage = selectedPage.map { getCorrectedPagePosition(it) }

    val taskName = MutableLiveData("")

    val numberField = MutableLiveData<String>("")
    val requestFocusToNumberField: MutableLiveData<Boolean> = MutableLiveData()

    val processingGoods = MutableLiveData<List<Good>>()
    val processedGoods = MutableLiveData<List<Good>>()
    val searchGoods = MutableLiveData<List<Good>>()

    private val selectedItemOnCurrentTab: MutableLiveData<Boolean> = correctedSelectedPage
            .combineLatest(processedSelectionsHelper.selectedPositions)
            .combineLatest(searchSelectionsHelper.selectedPositions)
            .map {
                val tab = it?.first?.first?.toInt()
                val processedSelected = it?.first?.second?.isNotEmpty() == true
                val searchSelected = it?.second?.isNotEmpty() == true
                tab == GoodsListTab.PROCESSED.position && processedSelected || tab == GoodsListTab.SEARCH.position && searchSelected
            }

    val deleteButtonEnabled = selectedItemOnCurrentTab.map { it }
    val printButtonEnabled = selectedItemOnCurrentTab.map { it }

    val deleteButtonVisibility = correctedSelectedPage.map { it != GoodsListTab.PROCESSING.position }
    val printButtonVisibility = correctedSelectedPage.map { it != GoodsListTab.PROCESSING.position }

    init {
        viewModelScope.launch {
            requestFocusToNumberField.value = true

            // Тестовые данные
            /*processingGoods.value = taskManager.getTestGoodList(3)
            processedGoods.value = taskManager.getTestGoodList(28)
            searchGoods.value = taskManager.getTestGoodList(2)*/
        }
    }

    init {
        viewModelScope.launch {
            taskName.value = "${checkPriceTaskManager.getTaskType()} // ${checkPriceTaskManager.getTaskName()}"
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

    fun onClickPrint() {

    }

    fun onClickVideo() {
        navigator.openScanPriceScreen()
    }

    fun onClickItemPosition(position: Int) {
        navigator.openGoodInfoPcScreen()
    }

    fun getPagesCount(): Int {
        return if (checkPriceTaskManager.getTask()?.getDescription()?.taskNumber.isNullOrBlank()) 2 else 3
    }

    fun getCorrectedPagePosition(position: Int?): Int {
        return if (getPagesCount() == 3) position ?: 0 else (position ?: 0) + 1
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


