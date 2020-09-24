package com.lenta.bp12.features.open_task.good_details

import com.lenta.bp12.features.other.ItemBasketUi
import com.lenta.bp12.features.other.ItemCategory
import com.lenta.bp12.features.other.ItemCategoryUi
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.model.CategoryType
import com.lenta.bp12.model.pojo.extentions.*
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.orIfNull
import javax.inject.Inject

class GoodDetailsOpenViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: IOpenTaskManager

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
                            description = basket.getDescription(
                                    isDivBySection = task.type?.isDivBySection ?: false,
                                    isWholeSale = manager.isWholesaleTaskType
                            ),
                            quantity = "${basket.getQuantityFromGoodList()}"
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
                        quantity = "${itemCategory.quantity.dropZeros()} ${good?.commonUnits?.name}"
                )
            }
        }
    }

    val deleteEnabled = categorySelectionsHelper.selectedPositions.map {
        it?.isNotEmpty() ?: false
    }

    // -----------------------------

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickDelete() {
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
        }.orIfNull {
            Logg.e { "good null" }
            navigator.showInternalError(resource.goodNotFoundErrorMsg)
        }
    }

}