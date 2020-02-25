package com.lenta.bp12.features.good_details

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.features.task_composition.ItemBasketUi
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import javax.inject.Inject

class GoodDetailsViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator


    val basketSelectionsHelper = SelectionItemsHelper()
    val categorySelectionsHelper = SelectionItemsHelper()

    val title = MutableLiveData("000015 Левый носок")

    val deleteEnabled = MutableLiveData(false)

    val selectedPage = MutableLiveData(0)

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

    val categories by lazy {
        MutableLiveData(List(3) {
            ItemCategoryUi(
                    position = "${it + 1}",
                    type = "Test type ${it + 1}",
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