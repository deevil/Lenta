package com.lenta.bp12.features.task_composition

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.utilities.databinding.PageSelectionListener

class TaskCompositionViewModel : CoreViewModel(), PageSelectionListener {

    val selectedPage = MutableLiveData(0)

    val deleteEnabled = MutableLiveData(false)

    val saveEnabled = MutableLiveData(false)

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    // TODO: Implement the ViewModel
}


data class ItemGoodUi(
        val position: String,
        val name: String,
        val quantity: String
)

data class ItemBasketUi(
        val position: String,
        val name: String,
        val description: String,
        val quantity: String
)