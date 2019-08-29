package com.lenta.bp14.features.not_exposed.good_info

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.utilities.databinding.PageSelectionListener
import kotlinx.coroutines.launch

class GoodInfoNeViewModel : CoreViewModel(), PageSelectionListener {

    val balances: MutableLiveData<List<StockInfoUi>> = MutableLiveData()

    init {
        viewModelScope.launch {
            createTestData()
        }

    }

    private fun createTestData() {
        balances.value = List(100) {
            StockInfoUi(
                    number = it,
                    storage = "0010",
                    quantity = "${10 + it} шт."
            )
        }

    }

    val selectedPage = MutableLiveData(0)

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun getTitle(): String {
        return "???"
    }

}

data class StockInfoUi(
        val number: Int,
        val storage: String,
        val quantity: String
)
