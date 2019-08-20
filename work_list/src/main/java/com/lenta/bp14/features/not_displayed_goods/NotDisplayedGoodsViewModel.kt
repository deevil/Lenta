package com.lenta.bp14.features.not_displayed_goods

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.utilities.databinding.PageSelectionListener

class NotDisplayedGoodsViewModel : CoreViewModel(), PageSelectionListener {

    val selectedPage = MutableLiveData(0)

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickSave() {

    }

    fun getTitle(): String {
        return "???"
    }

}
