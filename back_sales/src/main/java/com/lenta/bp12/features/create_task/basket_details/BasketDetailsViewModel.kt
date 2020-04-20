package com.lenta.bp12.features.create_task.basket_details

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class BasketDetailsViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: ICreateTaskManager


    val selectionsHelper = SelectionItemsHelper()

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

    val baskets by lazy {
        good.map { good ->
            task.value?.let { task ->
                val list = task.baskets.filter {
                    it.section == good?.section && it.type == good.type && it.control == good.control
                }

                list.mapIndexed { index, basket ->
                    ItemBasketUi(
                            basket = basket,
                            position = "${list.size - index}",
                            name = "Корзина ${manager.getBasketPosition(basket)}",
                            description = basket.getDescription(),
                            quantity = task.getQuantityByBasket(basket).dropZeros()
                    )
                }
            }
        }
    }

    val deleteEnabled = MutableLiveData(false)

    // -----------------------------

    fun onClickDelete() {
        val basketList = mutableListOf<Basket>()
        selectionsHelper.selectedPositions.value?.map { position ->
            baskets.value?.get(position)?.basket?.let {
                basketList.add(it)
            }
        }

        selectionsHelper.clearPositions()
        manager.deleteBaskets(basketList)
    }

}


data class ItemBasketUi(
        val basket: Basket,
        val position: String,
        val name: String,
        val description: String,
        val quantity: String
)