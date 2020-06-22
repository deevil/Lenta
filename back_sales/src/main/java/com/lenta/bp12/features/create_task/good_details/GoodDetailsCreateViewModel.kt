package com.lenta.bp12.features.create_task.good_details

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.CategoryType
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

    var countTab: Int? = null

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
        good.map { good ->
            val categories = mutableListOf<ItemCategory>()
            good?.getMarkQuantity()?.let { quantity ->
                if (quantity > 0) {
                    categories.add(ItemCategory(
                            type = CategoryType.MARK,
                            quantity = quantity
                    ))
                }
            }

            good?.getPartQuantity()?.let { quantity ->
                if (quantity > 0) {
                    categories.add(ItemCategory(
                            type = CategoryType.PART,
                            quantity = quantity
                    ))
                }
            }

            categories.mapIndexed { index, itemCategory ->
                ItemCategoryUi(
                        position = "${index + 1}",
                        type = itemCategory.type.description,
                        quantity = "${itemCategory.quantity.dropZeros()} ${good?.units?.name}"
                )
            }
        }
    }

    val deleteEnabled = selectedPage.combineLatest(basketSelectionsHelper.selectedPositions).combineLatest(categorySelectionsHelper.selectedPositions).map {
        it?.let {
            val tab = it.first.first
            val isBasketSelected = it.first.second.isNotEmpty()
            val isCategorySelected = it.second.isNotEmpty()

            tab == 0 && isBasketSelected || tab == 1 && isCategorySelected
        }
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
                    good.value?.let { changedGood ->
                        categorySelectionsHelper.selectedPositions.value?.map { position ->
                            categories.value?.get(position)?.type?.let { category ->
                                when (category) {
                                    CategoryType.MARK.description -> changedGood.removeAllMark()
                                    CategoryType.PART.description -> changedGood.removeAllPart()
                                }
                            }
                        }

                        manager.updateCurrentGood(changedGood)
                        manager.saveGoodInTask(changedGood)
                    }
                }
                else -> throw IllegalArgumentException("Wrong pager position!")
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

data class ItemCategory(
        val type: CategoryType,
        val quantity: Double
)

data class ItemCategoryUi(
        val position: String,
        val type: String,
        val quantity: String
)