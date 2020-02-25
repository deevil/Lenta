package com.lenta.bp12.features.task_composition

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import javax.inject.Inject

class TaskCompositionViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator


    val goodSelectionsHelper = SelectionItemsHelper()
    val basketSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val title = MutableLiveData("ВПП // Возврат от 10.12.2018 15:20")

    val deleteEnabled = MutableLiveData(false)

    val saveEnabled = MutableLiveData(false)

    // -----------------------------

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickDelete() {

    }

    fun onClickSave() {

    }

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