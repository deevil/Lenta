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
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.mapSkipNulls
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.lenta.shared.utilities.orIfNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    val currentPage = selectedPage.mapSkipNulls { TaskGoodsDetailsPage.values()[it] }

    init {
        launchUITryCatch {
            loadBasketSimpleList()
        }
    }

    val basketList by unsafeLazy {
        MutableLiveData<List<SimpleListItem>>()
    }

    val deleteEnabled = combineLatest(
            currentPage,
            basketSelectionHelper.selectedPositions,
            boxesSelectionHelper.selectedPositions
    ).mapSkipNulls { (currentPage, basketSelectedPositions, boxSelectedPositions) ->
        when (currentPage) {
            TaskGoodsDetailsPage.BASKETS -> basketSelectedPositions.orEmpty().isNotEmpty()
            TaskGoodsDetailsPage.BOXES -> boxSelectedPositions.orEmpty().isNotEmpty()
            else -> {
                Logg.e { "currentpage not acceptable" }
                false
            }
        }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
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

    fun onDeleteClick() = launchUITryCatch {
        when (currentPage.value) {
            TaskGoodsDetailsPage.BASKETS -> {
                val basketsOfProduct = taskBasketsRepository.getAll().filter { basket ->
                    basket.containsKey(product)
                }
                val doRemoveBaskets = basketSelectionHelper.selectedPositions.value.orEmpty().map { doRemoveBasketIndex ->
                    basketsOfProduct.getOrNull(doRemoveBasketIndex)
                }

                product?.let { product ->
                    doRemoveBaskets.forEach { doRemoveBasket ->
                        val indexOfRemovingBasket = doRemoveBasket?.index
                        indexOfRemovingBasket?.let { indexToRemove ->
                            taskBasketsRepository.removeProductFromBasket(indexToRemove, product)
                        }.orIfNull {
                            Logg.e { "baskets index is null" }
                        }
                    }
                }.orIfNull {
                    Logg.e { "product is null" }
                }

                loadBasketSimpleList()
                basketSelectionHelper.clearPositions()
            }
            TaskGoodsDetailsPage.BOXES -> {
                // TODO
            }
        }
    }

    private suspend fun loadBasketSimpleList() {
        val basketFreshList = withContext(Dispatchers.IO) {
            product?.let { product ->
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
            }.orEmpty()
        }
        basketList.value = basketFreshList
    }
}