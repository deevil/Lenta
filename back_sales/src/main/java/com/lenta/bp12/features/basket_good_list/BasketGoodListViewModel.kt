package com.lenta.bp12.features.basket_good_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import javax.inject.Inject

class BasketGoodListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    val selectionsHelper = SelectionItemsHelper()

    val title = MutableLiveData("Корзина 02: C-02/1HAW/O/ПП-325985")

    val deleteEnabled = MutableLiveData(false)

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

}

data class ItemGoodUi(
        val position: String,
        val name: String,
        val quantity: String
)