package com.lenta.bp14.features.price_check.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.data.model.Good
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch

class GoodInfoPcViewModel : CoreViewModel() {

    val good = MutableLiveData<Good>()

    init {
        viewModelScope.launch {
            good.value = Good(
                    id = 0,
                    material = "000000000000254128",
                    name = "Обои",
                    total = 5
            )
        }
    }
}
