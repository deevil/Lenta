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
import com.lenta.shared.utilities.Logg
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
            Logg.e { goodValue.toString() }
            val categories = mutableListOf<ItemCategory>()
            val markQuantity = goodValue.getMarkQuantity()
            if (markQuantity > ZERO_QUANTITY) {
                categories.add(
                        ItemCategory(
                                type = CategoryType.MARK,
                                quantity = markQuantity
                        )
                )
            }

            val partQuantity = goodValue.getPartQuantity()
            if (partQuantity > ZERO_QUANTITY) {
                categories.add(
                        ItemCategory(
                                type = CategoryType.PART,
                                quantity = partQuantity
                        )
                )
            }

            categories.mapIndexed { index, itemCategory ->
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
            when (page) {
                BASKETS_TAB -> handleDeleteBaskets()
                CATEGORIES_TAB -> handleDeleteCategories()
                else -> launchUITryCatch {
                    throw IllegalArgumentException("Wrong pager position!")
                }
            }
        }
    }

    private fun handleDeleteBaskets() {
        good.value?.let{
            val basketList = basketSelectionsHelper.selectedPositions.value?.mapNotNullTo(mutableListOf()) { position ->
                baskets.value?.get(position)?.basket
            }.orEmptyMutable()

            basketSelectionsHelper.clearPositions()
            manager.removeBaskets(basketList)
            manager.updateCurrentGood(it)
        }
    }

    private fun handleDeleteCategories() {
        good.value?.let { changedGood ->
            val categoriesValue = categories.value.orEmpty()
            if (categoriesValue.isNotEmpty()) {
                categorySelectionsHelper.selectedPositions.value?.forEach { position ->
                    categoriesValue.getOrNull(position)?.let { changedGood.processCategoryType(it) }
                }
            }

            categorySelectionsHelper.clearPositions()
            with(manager) {
                task.value?.let {
                    if (changedGood.isEmpty()) {
                        deleteGood(changedGood)
                        navigator.goBack()
                    } else {
                        updateCurrentGood(changedGood)
                        saveGoodInTask(changedGood)
                    }
                }
            }
        }.orIfNull {
            Logg.e { "good null" }
            navigator.showInternalError(resource.goodNotFoundErrorMsg)
        }
    }

    private fun Good.processCategoryType(item: ItemCategoryUi) {
        when (item.type) {
            CategoryType.MARK.description -> this.removeAllMark()
            CategoryType.PART.description -> this.removeAllPart()
        }
    }

    companion object {
        private const val BASKETS_TAB = 0
        private const val CATEGORIES_TAB = 1
    }
}

