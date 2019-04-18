package com.lenta.bp10.features.select_market

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch

class SelectMarketViewModel : CoreViewModel(), OnPositionClickListener {
    val markets: MutableLiveData<List<MarketUi>> = MutableLiveData()
    val marketsNames: MutableLiveData<List<String>> = markets.map { markets ->
        markets?.map { it.number }
    }
    val selectedPosition: MutableLiveData<Int> = MutableLiveData()
    val selectedAddress: MutableLiveData<String> = MutableLiveData()

    init {
        viewModelScope.launch {
            markets.value = listOf(MarketUi("01", "Петербург"),
                    MarketUi("02", "Москва")
            )
        }
    }

    override fun onClickPosition(position: Int) {
    }

    override fun onNothingSelected() {
    }

}

data class MarketUi(
        val number: String,
        val address: String

)