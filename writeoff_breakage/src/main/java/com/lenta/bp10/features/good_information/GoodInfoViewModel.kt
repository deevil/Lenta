package com.lenta.bp10.features.good_information

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.viewmodel.CoreViewModel

class GoodInfoViewModel : CoreViewModel() {
    private lateinit var goodCode: String

    fun setGoodCode(goodCode: String) {
        this.goodCode = goodCode
    }

    val goodTitle = MutableLiveData("")


}
