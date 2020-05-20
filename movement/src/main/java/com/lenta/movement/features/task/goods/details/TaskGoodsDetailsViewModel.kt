package com.lenta.movement.features.task.goods.details

import androidx.lifecycle.MutableLiveData
import com.lenta.movement.models.ProductInfo
import com.lenta.movement.models.SimpleListItem
import com.lenta.movement.models.repositories.ITaskBasketsRepository
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.mapSkipNulls
import javax.inject.Inject

class TaskGoodsDetailsViewModel : CoreViewModel(), PageSelectionListener {

    var product: ProductInfo? = null

    @Inject
    lateinit var taskBasketsRepository: ITaskBasketsRepository

    val basketSelectionHelper = SelectionItemsHelper()

    val boxesSelectionHelper = SelectionItemsHelper()

    val selectedPagePosition = MutableLiveData(0)
    val currentPage = selectedPagePosition.mapSkipNulls { TaskGoodsDetailsPage.values()[it] }

    val basketList by lazy { MutableLiveData(getBasketSimpleList()) }
    val boxList = MutableLiveData(listOf<SimpleListItem>())

    val deleteEnabled = combineLatest(
        currentPage,
        basketSelectionHelper.selectedPositions,
        boxesSelectionHelper.selectedPositions
    )
        .mapSkipNulls { (currentPage, basketSelectedPositions, boxSelectedPositions) ->
            when (currentPage!!) {
                TaskGoodsDetailsPage.BASKETS -> basketSelectedPositions.orEmpty().isNotEmpty()
                TaskGoodsDetailsPage.BOXES -> boxSelectedPositions.orEmpty().isNotEmpty()
            }
        }

    override fun onPageSelected(position: Int) {
        selectedPagePosition.value = position
    }

    fun getAvailablePages(): List<TaskGoodsDetailsPage> {
        return if (product!!.isExcise) {
            listOf(TaskGoodsDetailsPage.BASKETS, TaskGoodsDetailsPage.BOXES)
        } else {
            listOf(TaskGoodsDetailsPage.BASKETS)
        }
    }

    fun getTitle(): String {
        return "${product?.getMaterialLastSix()} ${product?.description}"
    }

    fun onDeleteClick() {
        when (currentPage.value!!) {
            TaskGoodsDetailsPage.BASKETS -> {
                val basketsOfProduct = taskBasketsRepository.getAll().filter { basket ->
                    basket.containsKey(product!!)
                }
                val doRemoveBaskets = basketSelectionHelper.selectedPositions.value.orEmpty().map { doRemoveBasketIndex ->
                    basketsOfProduct[doRemoveBasketIndex]
                }

                doRemoveBaskets.forEach { doRemoveBasket ->
                    taskBasketsRepository.removeProduct(doRemoveBasket.index, product!!)
                }

                basketList.postValue(getBasketSimpleList())
            }
            TaskGoodsDetailsPage.BOXES -> {
                // TODO
            }
        }
    }

    private fun getBasketSimpleList(): List<SimpleListItem> {
        return taskBasketsRepository.getAll()
            .filter { basket ->
                basket.containsKey(product!!)
            }
            .mapIndexed { index, basket ->
                SimpleListItem(
                    number = index + 1,
                    title = "Корзина ${basket.index}",
                    subtitle = "",
                    countWithUom = "${basket[product!!]}"
                )
            }
    }
}