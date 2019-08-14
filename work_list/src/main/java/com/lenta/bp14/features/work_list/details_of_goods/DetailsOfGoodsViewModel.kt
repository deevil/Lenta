package com.lenta.bp14.features.work_list.details_of_goods

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.PageSelectionListener

class DetailsOfGoodsViewModel : CoreViewModel(), PageSelectionListener {

    val enabledDeleteButton: MutableLiveData<Boolean> = MutableLiveData(false)
    val selectedPage = MutableLiveData(0)

    fun getTitle(): String? {
        return "???"
    }

    fun onClickDelete() {

    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }
}
