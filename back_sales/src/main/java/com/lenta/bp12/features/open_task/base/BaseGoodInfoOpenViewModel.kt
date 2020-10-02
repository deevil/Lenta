package com.lenta.bp12.features.open_task.base

import com.lenta.bp12.features.base.BaseGoodInfoViewModel
import com.lenta.bp12.features.open_task.base.interfaces.IBaseGoodInfoOpenViewModel
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.model.pojo.open_task.TaskOpen
import com.lenta.bp12.platform.ZERO_QUANTITY
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy

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
                        append(resource.fromPlannedQuantity(it.planQuantity.dropZeros()))
                    }
                    append(" ")
                    append(it.commonUnits.name)
                }
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

    override fun isFactQuantityMoreThanPlanned(): Boolean {
        val totalQuantity = good.value?.getTotalQuantity() ?: ZERO_QUANTITY
        val quantityValue = quantity.value ?: ZERO_QUANTITY
        val quantitySum = quantityValue + totalQuantity
        val isFactQuantityMoreThanPlanned = quantitySum > plannedQuantity
        return isPlannedQuantityMoreThanZero && isFactQuantityMoreThanPlanned
    }

    companion object {
        private const val FROM_STRING = "из"
    }
}

