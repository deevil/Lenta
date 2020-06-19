package com.lenta.bp12.features.create_task.good_details

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class GoodDetailsCreateViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: ICreateTaskManager


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
                val list = task.baskets.filter {
                    it.section == good?.section && it.matype == good.matype && it.control == good.control
                }

                list.mapIndexed { index, basket ->
                    ItemBasketUi(
                            basket = basket,
                            position = "${list.size - index}",
                            name = "Корзина ${manager.getBasketPosition(basket)}",
                            description = basket.getDescription(task.properties.isDivBySection),
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

    val deleteEnabled = selectedPage.combineLatest(basketSelectionsHelper.selectedPositions).combineLatest(categorySelectionsHelper.selectedPositions).map {
        val tab = it!!.first.first
        val isBasketSelected = it.first.second.isNotEmpty()
        val isCategorySelected = it.second.isNotEmpty()

        tab == 0 && isBasketSelected || tab == 1 && isCategorySelected
    }

    // -----------------------------

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickDelete() {
        selectedPage.value?.let { page ->
            when (page) {
                0 -> {
                    val basketList = mutableListOf<Basket>()
                    basketSelectionsHelper.selectedPositions.value?.map { position ->
                        baskets.value?.get(position)?.basket?.let {
                            basketList.add(it)
                        }
                    }

                    basketSelectionsHelper.clearPositions()
                    manager.removeBaskets(basketList)
                }
                1 -> {
                    // todo Удаление категорий, когда они будут поноценно реализованы
                    // ...

                }
            }
        }
    }

}


data class ItemBasketUi(
        val basket: Basket,
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