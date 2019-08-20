package com.lenta.bp9.features.goods_list

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.utilities.databinding.PageSelectionListener

class GoodsListViewModel : CoreViewModel(), PageSelectionListener {

    val selectedPage = MutableLiveData(0)

    fun getTitle(): String {
        return "???"
    }

    fun onScanResult(data: String) {
        return
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

}
