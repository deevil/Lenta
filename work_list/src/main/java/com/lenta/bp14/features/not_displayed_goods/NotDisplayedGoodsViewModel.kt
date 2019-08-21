package com.lenta.bp14.features.not_displayed_goods

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotDisplayedGoodsViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    val eanCode: MutableLiveData<String> = MutableLiveData("")
    val notProcessedGoods: MutableLiveData<List<GoodsUi>> = MutableLiveData()
    val processedGoods: MutableLiveData<List<ProcessedGoodsUi>> = MutableLiveData()
    val filteredGoods: MutableLiveData<List<GoodsUi>> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()


    init {
        viewModelScope.launch {
            createTestData()
        }
    }

    private fun createTestData() {
        notProcessedGoods.value = List(100) {
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

        filteredGoods.value = List(100) {
            GoodsUi(
                    it,
                    "000022 Селедка $it"
            )
        }

    }

    val selectedPage = MutableLiveData(0)

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


}

data class ProcessedGoodsUi(
        val number: Int,
        val name: String,
        val quantity: String
)

data class GoodsUi(
        val number: Int,
        val name: String
)
