package com.lenta.bp12.features.task_composition

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.features.basket_good_list.ItemGoodUi
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import javax.inject.Inject

class TaskCompositionViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator


    val goodSelectionsHelper = SelectionItemsHelper()
    val basketSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val title by lazy {
        "ВПП // Возврат от 10.12.2018 15:20"
    }

    val deleteEnabled = MutableLiveData(false)

    val saveEnabled = MutableLiveData(false)

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
    }

    val goods by lazy {
        MutableLiveData(List(3) {
            ItemGoodUi(
                    position = "${it + 1}",
                    name = "Test name ${it + 1}",
                    quantity = (1..15).random().toString()
            )
        })
    }

    val baskets by lazy {
        MutableLiveData(List(3) {
            ItemBasketUi(
                    position = "${it + 1}",
                    name = "Test name ${it + 1}",
                    description = "Test description ${it + 1}",
                    quantity = (1..15).random().toString()
            )
        })
    }

    // -----------------------------

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickDelete() {

    }

    fun onClickSave() {

    }

    fun onClickItemPosition(position: Int) {

    }

    override fun onOkInSoftKeyboard(): Boolean {
        return true
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