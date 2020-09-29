package com.lenta.bp12.features.base

import com.lenta.bp12.features.other.ItemBasketUi
import com.lenta.bp12.features.other.ItemCategory
import com.lenta.bp12.features.other.ItemCategoryUi
import com.lenta.bp12.managers.interfaces.ITaskManager
import com.lenta.bp12.model.CategoryType
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.extentions.getDescription
import com.lenta.bp12.model.pojo.extentions.getPosition
import com.lenta.bp12.model.pojo.extentions.removeAllMark
import com.lenta.bp12.model.pojo.extentions.removeAllPart
import com.lenta.bp12.platform.ZERO_QUANTITY
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.orIfNull
import javax.inject.Inject

abstract class BaseGoodDetailsViewModel<T : ITaskManager<*>> : CoreViewModel(), PageSelectionListener {

    abstract var manager: T

    @Inject
    lateinit var resource: IResourceManager

    @Inject
    lateinit var navigator: IScreenNavigator

    val task by lazy {
        manager.currentTask
    }

    val good by lazy {
        manager.currentGood
    }

    val isGoodTobaccoOrExcise by unsafeLazy {
        good.value?.let {
            it.isExciseAlco() || it.isMarked()
        } ?: false
    }

    val basketSelectionsHelper = SelectionItemsHelper()

    val categorySelectionsHelper = SelectionItemsHelper()

    val baskets by lazy {
        task.combineLatest(good).mapSkipNulls {
            val (task, good) = it

            val baskets = task.getBasketsByGood(good)

            baskets.mapIndexed { index, basket ->
                ItemBasketUi(
                        basket = basket,
                        position = "${baskets.size - index}",
                        name = resource.basket("${basket.getPosition()}"),
                        description = basket.getDescription(
                                isDivBySection = task.type?.isDivBySection == true,
                                isWholeSale = manager.isWholesaleTaskType
                        ),
                        quantity = "${basket.goods[good].dropZeros()} ${good.commonUnits.name}"
                )
            }
        }
    }

    val title by lazy {
        good.map { good ->
            good?.getNameWithMaterial()
        }
    }

    val categories by lazy {
        good.mapSkipNulls { goodValue ->
            val categories = mutableListOf<ItemCategory>()
            with(categories) {
                addMarksIfNeeded(goodValue)
                addPartQuantityIfNeeded(goodValue)
                mapIndexed { index, itemCategory ->
                    val quantity = itemCategory.quantity.dropZeros()
                    val units = goodValue.commonUnits.name

                    ItemCategoryUi(
                            position = "${index + 1}",
                            type = itemCategory.type.description,
                            quantity = "$quantity $units"
                    )
                }
            }
        }
    }

    val deleteEnabled = selectedPage.combineLatest(basketSelectionsHelper.selectedPositions)
            .combineLatest(categorySelectionsHelper.selectedPositions)
            .mapSkipNulls {
                val tab = it.first.first
                val isBasketSelected = it.first.second.isNotEmpty()
                val isCategorySelected = it.second.isNotEmpty()

                (tab == BASKETS_TAB && isBasketSelected) || (tab == CATEGORIES_TAB && isCategorySelected)
            }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickDelete() {
        selectedPage.value?.let { page ->
            good.value?.let { good ->
                when (page) {
                    BASKETS_TAB -> handleDeleteBaskets(good)
                    CATEGORIES_TAB -> handleDeleteCategories(good)
                    else -> launchUITryCatch {
                        throw IllegalArgumentException("Wrong pager position!")
                    }
                }
            }.orIfNull {
                navigator.showInternalError(resource.goodNotFoundErrorMsg)
            }
        }.orIfNull {
            navigator.showInternalError(resource.pageNotFoundErrorMsg)
        }
    }

    private fun handleDeleteBaskets(good: Good) {
        val basketList = basketSelectionsHelper.selectedPositions.value?.mapNotNullTo(mutableListOf()) { position ->
            baskets.value?.get(position)?.basket
        }.orEmptyMutable()

        basketSelectionsHelper.clearPositions()
        manager.removeBaskets(basketList)
        manager.updateCurrentGood(good)
    }

    private fun handleDeleteCategories(good: Good) {
        val categoriesValue = categories.value.orEmpty()
        if (categoriesValue.isNotEmpty()) {
            categorySelectionsHelper.selectedPositions.value?.forEach { position ->
                categoriesValue.getOrNull(position)?.let { good.processCategoryType(it) }
            }
        }

        categorySelectionsHelper.clearPositions()
        with(manager) {
            if (good.isEmpty()) {
                deleteGood(good)
            } else {
                updateCurrentGood(good)
                saveGoodInTask(good)
            }
        }
    }

    private fun Good.processCategoryType(item: ItemCategoryUi) {
        when (item.type) {
            CategoryType.MARK.description -> this.removeAllMark()
            CategoryType.PART.description -> this.removeAllPart()
        }
    }

    private fun MutableList<ItemCategory>.addMarksIfNeeded(good: Good) {
        val markQuantity = good.getMarkQuantity()
        if (markQuantity > ZERO_QUANTITY) {
            val item = ItemCategory(
                    type = CategoryType.MARK,
                    quantity = markQuantity
            )
            add(item)
        }
    }

    private fun MutableList<ItemCategory>.addPartQuantityIfNeeded(good: Good) {
        val partQuantity = good.getPartQuantity()
        if (partQuantity > ZERO_QUANTITY) {
            val item = ItemCategory(
                    type = CategoryType.PART,
                    quantity = partQuantity
            )
            add(item)
        }
    }

    fun countTab(): Int {
        return good.value?.let {
            when {
                (!manager.isWholesaleTaskType && it.isCommon()) -> ONE_TAB
                (manager.isWholesaleTaskType && it.isGoodCommonOrMarkedOrAlco()) -> ONE_TAB
                else -> TWO_TABS
            }
        }.orIfNull { ONE_TAB }
    }

    companion object {
        private const val BASKETS_TAB = 0
        private const val CATEGORIES_TAB = 1

        private const val ONE_TAB = 1
        private const val TWO_TABS = 2
    }
}

