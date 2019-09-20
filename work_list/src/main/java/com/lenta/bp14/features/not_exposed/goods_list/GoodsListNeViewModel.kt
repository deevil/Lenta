package com.lenta.bp14.features.not_exposed.goods_list

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.data.GoodsListTab
import com.lenta.bp14.models.data.pojo.Good
import com.lenta.bp14.models.not_exposed_products.INotExposedProductsTask
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
    lateinit var task: INotExposedProductsTask


    val processedSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val correctedSelectedPage = selectedPage.map { getCorrectedPagePosition(it) }

    val taskName = MutableLiveData("Невыставленный товар от 23.07.19 23:15")

    val numberField: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToNumberField: MutableLiveData<Boolean> = MutableLiveData()

    val processingGoods = MutableLiveData<List<Good>>()
    val processedGoods = MutableLiveData<List<NotExposedProductUi>>()
    val searchGoods = MutableLiveData<List<Good>>()

    private val selectedItemOnCurrentTab: MutableLiveData<Boolean> = correctedSelectedPage
            .combineLatest(processedSelectionsHelper.selectedPositions)
            .map {
                val tab = it?.first?.toInt()
                val processedSelected = it?.second?.isNotEmpty() == true
                tab == GoodsListTab.PROCESSED.position && processedSelected || tab == GoodsListTab.SEARCH.position
            }

    val thirdButtonEnabled = selectedItemOnCurrentTab.map { it }
    val saveButtonEnabled = processingGoods.map { it?.isNotEmpty() ?: false }

    val thirdButtonVisibility = correctedSelectedPage.map { it != GoodsListTab.PROCESSING.position }

    init {
        viewModelScope.launch {
            requestFocusToNumberField.value = true

            processedGoods.value = List(10) {
                NotExposedProductUi(
                        position = it,
                        matNr = "000021",
                        name = "Селедка $it",
                        quantity = "10 кг",
                        isEmptyPlaceFramed = null
                )
            }


        }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    override fun onOkInSoftKeyboard(): Boolean {
        return false
    }


    fun onClickSave() {

    }

    private fun onClickDelete() {
        processedGoods.value = emptyList()

    }

    fun onClickThirdButton() {
        when (correctedSelectedPage.value) {
            1 -> onClickDelete()
            2 -> navigator.openSearchFilterWlScreen()
        }

    }

    fun onClickItemPosition(position: Int) {
        navigator.openGoodInfoNeScreen()
    }

    fun getPagesCount(): Int {
        return if (task.isFreeMode()) 2 else 3
    }

    fun getCorrectedPagePosition(position: Int?): Int {
        return if (getPagesCount() == 3) position ?: 0 else (position ?: 0) + 1
    }

    fun onDigitPressed(digit: Int) {
        numberField.postValue(numberField.value ?: "" + digit)
        requestFocusToNumberField.value = true
    }

}


data class NotExposedProductUi(
        val position: Int,
        val matNr: String,
        val name: String,
        val quantity: String?,
        val isEmptyPlaceFramed: Boolean?
)
