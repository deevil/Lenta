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

    val processingGoods = MutableLiveData<List<Good>>(getTestItems())
    val processedGoods = MutableLiveData<List<Good>>(getTestItems())
    val searchGoods = MutableLiveData<List<Good>>(getTestItems())

    val eanCode: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()


    /*init {
        viewModelScope.launch {
            //createTestData()
        }
    }*/

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

    /*private fun createTestData() {
        processingGoods.value = List(100) {
            GoodsUi(
                    it,
                    "000021 Горбуша $it"
            )
        }

        processedGoods.value = List(100) {
            ProcessedGoodsUi(
                    it,
                    "000021 Горбуша ${it + 100}",
                    "20 шт."
            )
        }

        searchGoods.value = List(100) {
            GoodsUi(
                    it,
                    "000022 Селедка $it"
            )
        }

    }*/



    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickSave() {

    }

    fun getTitle(): String {
        return "???"
    }

    override fun onOkInSoftKeyboard(): Boolean {
        return true

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

/*data class ProcessedGoodsUi(
        val number: Int,
        val name: String,
        val quantity: String
)

data class GoodsUi(
        val number: Int,
        val name: String
)*/
