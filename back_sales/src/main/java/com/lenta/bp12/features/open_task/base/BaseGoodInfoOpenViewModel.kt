package com.lenta.bp12.features.open_task.base
import androidx.lifecycle.switchMap
import com.lenta.bp12.features.base.BaseGoodInfoViewModel
import com.lenta.bp12.features.open_task.base.interfaces.IBaseGoodInfoOpenViewModel
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.extentions.getQuantityOfGood
import com.lenta.bp12.model.pojo.open_task.TaskOpen
import com.lenta.bp12.platform.ZERO_QUANTITY
import com.lenta.shared.utilities.extentions.*

/** Класс содержащий общую логику для:
* @see com.lenta.bp12.features.open_task.marked_good_info.MarkedGoodInfoOpenViewModel
* @see com.lenta.bp12.features.open_task.good_info.GoodInfoOpenViewModel
 * */
abstract class BaseGoodInfoOpenViewModel : BaseGoodInfoViewModel<TaskOpen, IOpenTaskManager>(), IBaseGoodInfoOpenViewModel {

    val isWholesale by lazy {
        manager.isWholesaleTaskType
    }

    override val totalWithUnits by lazy {
        totalQuantity.map { quantity ->
            good.value?.let {
                buildString {
                    append(quantity.dropZeros())
                    if (isPlannedQuantityMoreThanZero) {
                        append(" $FROM_STRING ${it.planQuantity.dropZeros()}")
                    }
                    append(" ")
                    append(it.commonUnits.name)
                }
            }
        }
    }

    /**
     * Корзины
     * */
    override val basketNumber by unsafeLazy {
        good.combineLatest(quantity).combineLatest(isProviderSelected).switchMap {
            asyncTryCatchLiveData {
                val isProviderSelected = it.second

                if (isProviderSelected) {
                    task.value?.let { task ->
                        getBasket()?.let { basket ->
                            "${basket.index}"
                        } ?: "${task.baskets.size + 1}"
                    }.orEmpty()
                } else ""
            }
        }
    }

    override val basketQuantity by unsafeLazy {
        good.combineLatest(quantity).combineLatest(isProviderSelected).switchMap {
            asyncTryCatchLiveData {
                val good = it.first.first
                val enteredQuantity = it.first.second
                val isProviderSelected = it.second

                val result = if (isProviderSelected) {
                    getBasket()?.getQuantityOfGood(good)?.sumWith(enteredQuantity)
                            ?: enteredQuantity
                } else ZERO_QUANTITY

                result
            }
        }
    }

    override val closeEnabled by unsafeLazy {
        applyEnabled.map { it }
    }

    /**
     * Плановое количество
     * */

    val plannedQuantity by unsafeLazy {
        good.value?.planQuantity ?: ZERO_QUANTITY
    }

    val isPlannedQuantityMoreThanZero by unsafeLazy {
        good.value?.planQuantity?.let {
            it > 0
        } ?: false
    }

    override suspend fun getBasket(): Basket? {
        val good = good.value
        return good?.let {
            manager.getBasket(it.provider.code.orEmpty(), it, false)
        }
    }

    override fun isFactQuantityMoreThanPlanned(): Boolean {
        val totalQuantity = good.value?.getTotalQuantity() ?: 0.0
        val quantityValue = quantity.value ?: 0.0
        val quantitySum = quantityValue + totalQuantity
        val isFactQuantityMoreThanPlanned =  quantitySum > plannedQuantity
        return isPlannedQuantityMoreThanZero && isFactQuantityMoreThanPlanned
    }

    companion object {
        private const val FROM_STRING = "из"
    }
}

