package com.lenta.bp12.features.basket_good_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import javax.inject.Inject

class BasketGoodListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    val selectionsHelper = SelectionItemsHelper()

    val title by lazy {
        "Корзина 02: C-02/1HAW/O/ПП-325985"
    }

    val deleteEnabled = MutableLiveData(false)

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

    // -----------------------------

    fun onClickDelete() {

    }

    fun onClickProperties() {

    }

    fun onClickNext() {

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