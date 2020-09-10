package com.lenta.bp12.features.create_task.good_details

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.features.other.ItemBasketUi
import com.lenta.bp12.features.other.ItemCategory
import com.lenta.bp12.features.other.ItemCategoryUi
import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import com.lenta.bp12.model.CategoryType
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.extentions.getDescription
import com.lenta.bp12.model.pojo.extentions.getPosition
import com.lenta.bp12.model.pojo.extentions.removeAllMark
import com.lenta.bp12.model.pojo.extentions.removeAllPart
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
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

    @Inject
    lateinit var resource: IResourceManager


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
        task.combineLatest(good).map {
            it?.let {
                val (task, good) = it

                val baskets = task.getBasketsByGood(good)

                baskets.mapIndexed { index, basket ->
                    ItemBasketUi(
                            basket = basket,
                            position = "${baskets.size - index}",
                            name = resource.basket("${basket.getPosition()}"),
                            description = basket.getDescription(task.type.isDivBySection),
                            quantity = "${task.getCountByBasket(basket)}"
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
                val quantity = itemCategory.quantity.dropZeros()
                val units = good?.commonUnits?.name

                ItemCategoryUi(
                        position = "${index + 1}",
                        type = itemCategory.type.description,
                        quantity = "$quantity $units"
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
                    basketSelectionsHelper.selectedPositions.value?.mapNotNullTo(basketList) { position ->
                        baskets.value?.get(position)?.basket
                    }

                    basketSelectionsHelper.clearPositions()
                    manager.removeBaskets(basketList)
                }
                1 -> {
                    good.value?.let { changedGood ->
                        categorySelectionsHelper.selectedPositions.value?.forEach { position ->
                            categories.value?.get(position)?.type?.let { category ->
                                when (category) {
                                    CategoryType.MARK.description -> changedGood.removeAllMark()
                                    CategoryType.PART.description -> changedGood.removeAllPart()
                                }
                            }
                        }

                        categorySelectionsHelper.clearPositions()
                        manager.updateCurrentGood(changedGood)
                        manager.saveGoodInTask(changedGood)
                    }
                }
                else -> throw IllegalArgumentException("Wrong pager position!")
            }
        }
    }

}