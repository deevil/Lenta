package com.lenta.bp12.features.create_task.base

import androidx.lifecycle.switchMap
import com.lenta.bp12.features.base.BaseGoodInfoViewModel
import com.lenta.bp12.features.create_task.base.interfaces.IBaseGoodInfoCreateViewModel
import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.create_task.TaskCreate
import com.lenta.bp12.model.pojo.extentions.getQuantityOfGood
import com.lenta.bp12.platform.ZERO_QUANTITY
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.orIfNull

/**
 * Класс содержащий общую логику для
 * @see com.lenta.bp12.features.create_task.good_info.GoodInfoCreateViewModel
 * @see com.lenta.bp12.features.create_task.marked_good_info.MarkedGoodInfoCreateViewModel
 * */
abstract class BaseGoodInfoCreateViewModel : BaseGoodInfoViewModel<TaskCreate, ICreateTaskManager>(), IBaseGoodInfoCreateViewModel {

    override val totalWithUnits by unsafeLazy {
        totalQuantity.map { quantity ->
            "${quantity.dropZeros()} ${good.value?.commonUnits?.name}"
        }
    }

    /**
     * Корзины
     * */
    override val basketNumber by unsafeLazy {
        good.combineLatest(quantity)
                .combineLatest(isProviderSelected)
                .switchMap {
                    asyncTryCatchLiveData {
                        val good = it.first.first
                        val isProviderSelected = it.second

                        good.takeIf { isProviderSelected }
                                ?.run { getBasket(this)?.index?.toString() }
                                .orIfNull {
                                    task.value?.baskets?.size?.plus(1)?.toString()
                                }.orEmpty()
                    }
                }
    }

    override val basketQuantity by unsafeLazy {
        good.combineLatest(quantity)
                .combineLatest(isProviderSelected)
                .switchMap {
                    asyncTryCatchLiveData {
                        val good = it.first.first
                        val enteredQuantity = it.first.second
                        val isProviderSelected = it.second

                        good.takeIf { isProviderSelected }?.run {
                            getBasket(this)?.run {
                                getQuantityOfGood(good).sumWith(enteredQuantity)
                            }.orIfNull {
                                enteredQuantity
                            }
                        }.orIfNull { ZERO_QUANTITY }
                    }
                }
    }

    /**
    Список поставщиков
     */

    override val closeEnabled by unsafeLazy {
        basketQuantity.map { basketQuantityValue ->
            basketQuantityValue?.let { it > 0 }
        }
    }

    override suspend fun getBasket(good: Good): Basket? {
        return manager.getBasket(ProviderInfo.getEmptyCode(), good, false)
    }

    override fun updateData() {
        val good = good.value
        if (manager.isWasAddedProvider && good != null) {
            updateProviders(good.providers)
            providerPosition.value = 1
            manager.isWasAddedProvider = false
        }
    }
}

