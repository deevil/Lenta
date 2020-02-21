package com.lenta.bp12.features.good_details

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.utilities.databinding.PageSelectionListener

class GoodDetailsViewModel : CoreViewModel(), PageSelectionListener {

    val selectedPage = MutableLiveData(0)

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    // TODO: Implement the ViewModel
}


data class ItemBasketUi(
        val position: String,
        val name: String,
        val description: String,
        val quantity: String
)

data class ItemCategoryUi(
        val position: String,
        val type: String,
        val quantity: String
)