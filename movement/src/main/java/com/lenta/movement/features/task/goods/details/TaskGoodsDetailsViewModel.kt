package com.lenta.movement.features.task.goods.details

import androidx.lifecycle.MutableLiveData
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.ProductInfo
import com.lenta.movement.models.SimpleListItem
import com.lenta.movement.models.repositories.ITaskBasketsRepository
import com.lenta.movement.platform.IFormatter
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.asyncLiveData
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.mapSkipNulls
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.lenta.shared.utilities.orIfNull
import javax.inject.Inject

class TaskGoodsDetailsViewModel : CoreViewModel(), PageSelectionListener {

    var product: ProductInfo? = null

    @Inject
    lateinit var taskManager: ITaskManager

    @Inject
    lateinit var taskBasketsRepository: ITaskBasketsRepository

    @Inject
    lateinit var formatter: IFormatter

    val basketSelectionHelper = SelectionItemsHelper()

    val boxesSelectionHelper = SelectionItemsHelper()

    val selectedPagePosition = MutableLiveData(0)
    val currentPage = selectedPagePosition.mapSkipNulls { TaskGoodsDetailsPage.values()[it] }

    val basketList by unsafeLazy {
        asyncLiveData<List<SimpleListItem>> {
            val listOfItems = getBasketSimpleList()
            emit(listOfItems)
        }
    }
    val boxList = MutableLiveData(listOf<SimpleListItem>())

    val deleteEnabled = combineLatest(
            currentPage,
            basketSelectionHelper.selectedPositions,
            boxesSelectionHelper.selectedPositions
    )
            .mapSkipNulls { (currentPage, basketSelectedPositions, boxSelectedPositions) ->
                when (currentPage) {
                    TaskGoodsDetailsPage.BASKETS -> basketSelectedPositions.orEmpty().isNotEmpty()
                    TaskGoodsDetailsPage.BOXES -> boxSelectedPositions.orEmpty().isNotEmpty()
                    else -> {
                        Logg.e { "u" }
                        false
                    }
                }
            }

    override fun onPageSelected(position: Int) {
        selectedPagePosition.value = position
    }

    fun getAvailablePages(): List<TaskGoodsDetailsPage> {
        return product?.let { productInfo ->
            if (productInfo.isExcise) {
                listOf(TaskGoodsDetailsPage.BASKETS, TaskGoodsDetailsPage.BOXES)
            } else {
                listOf(TaskGoodsDetailsPage.BASKETS)
            }
        }.orIfNull {
            Logg.e { "product is null" }
            listOf()
        }
    }

    fun getTitle(): String {
        return "${product?.getMaterialLastSix()} ${product?.description}"
    }

    fun onDeleteClick() {
        when (currentPage.value) {
            TaskGoodsDetailsPage.BASKETS -> {
                val basketsOfProduct = taskBasketsRepository.getAll().filter { basket ->
                    basket.containsKey(product)
                }
                val doRemoveBaskets = basketSelectionHelper.selectedPositions.value.orEmpty().map { doRemoveBasketIndex ->
                    basketsOfProduct[doRemoveBasketIndex]
                }

                doRemoveBaskets.forEach { doRemoveBasket ->
                    product?.let { product ->
                        taskBasketsRepository.removeProductFromBasket(doRemoveBasket.index, product)
                    }
                }

         //       basketList.postValue(getBasketSimpleList())
                basketSelectionHelper.clearPositions()
            }
            TaskGoodsDetailsPage.BOXES -> {
                // TODO
            }
        }
    }

    private suspend fun getBasketSimpleList(): List<SimpleListItem> {
        return product?.let { product ->
            taskBasketsRepository.getAll()
                    .filter { basket ->
                        basket.containsKey(product)
                    }
                    .mapIndexed { index, basket ->
                        SimpleListItem(
                                number = index + 1,
                                title = formatter.getBasketName(basket),
                                subtitle = formatter.getBasketDescription(
                                        basket,
                                        taskManager.getTask(),
                                        taskManager.getTaskSettings()
                                ),
                                countWithUom = "${basket[product]}",
                                isClickable = false
                        )
                    }
        } ?: listOf()
    }
}