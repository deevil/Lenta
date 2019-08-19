package com.lenta.bp14.features.price_check.goods_list

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.utilities.databinding.PageSelectionListener

class GoodsListPcViewModel : CoreViewModel(), PageSelectionListener {

    val selectedPage = MutableLiveData(0)

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    // TODO: Implement the ViewModel
}
