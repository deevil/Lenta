package com.lenta.bp14.features.good_info

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.utilities.databinding.PageSelectionListener

class GoodInfoViewModel : CoreViewModel(), PageSelectionListener {

    val selectedPage = MutableLiveData(0)

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    // TODO: Implement the ViewModel
}
