package com.lenta.bp12.features.good_details

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.ITaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class GoodDetailsViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: ITaskManager


    val basketSelectionsHelper = SelectionItemsHelper()

    val categorySelectionsHelper = SelectionItemsHelper()

    val task by lazy {
        manager.currentTask
    }

    val good by lazy {
        manager.currentGood
    }

    val title by lazy {
        good.map { good ->
            good?.getNameWithMaterial()
        }
    }

    val selectedPage = MutableLiveData(0)

    val baskets by lazy {
        good.map { good ->
            task.value?.let { task ->
                task.baskets.filter {
                    it.section == good?.section && it.type == good.type && it.control == good.control
                }.mapIndexed { index, basket ->
                    ItemBasketUi(
                            position = "${index + 1}",
                            name = "Корзина ${manager.getBasketPosition(basket)}",
                            description = basket.getDescription(),
                            quantity = task.getQuantityByBasket(basket).dropZeros()
                    )
                }
            }
        }
    }

    val categories by lazy {
        MutableLiveData(List(3) {
            ItemCategoryUi(
                    position = "${it + 1}",
                    type = "Test category ${it + 1}",
                    quantity = (1..15).random().toString()
            )
        })
    }

    val deleteEnabled = MutableLiveData(false)

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