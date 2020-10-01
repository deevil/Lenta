package com.lenta.bp12.features.create_task.base

import com.lenta.bp12.features.base.BaseGoodInfoViewModel
import com.lenta.bp12.features.create_task.base.interfaces.IBaseGoodInfoCreateViewModel
import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import com.lenta.bp12.model.pojo.create_task.TaskCreate
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy

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
    Список поставщиков
     */

    override val closeEnabled by unsafeLazy {
        basketQuantity.map { basketQuantityValue ->
            basketQuantityValue?.let { it > 0 }
        }
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

