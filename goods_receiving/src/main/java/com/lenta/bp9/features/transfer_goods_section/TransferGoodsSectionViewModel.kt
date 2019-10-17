package com.lenta.bp9.features.transfer_goods_section

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.utilities.databinding.PageSelectionListener

class TransferGoodsSectionViewModel : CoreViewModel(), PageSelectionListener {

    val selectedPage = MutableLiveData(0)

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    // TODO: Implement the ViewModel
}
