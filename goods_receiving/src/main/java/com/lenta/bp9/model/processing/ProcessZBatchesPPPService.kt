package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskMercuryDiscrepancies
import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.extentions.removeItemFromListWithPredicate
import com.lenta.shared.utilities.extentions.toStringFormatted
import javax.inject.Inject

@AppScope
class ProcessZBatchesPPPService
@Inject constructor() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    private lateinit var productInfo: TaskProductInfo
    private val currentProductDiscrepancies: ArrayList<TaskProductDiscrepancies> = ArrayList()

    fun newProcessZBatchesPPPService(initProductInfo: TaskProductInfo) : ProcessZBatchesPPPService? {
        return if (!initProductInfo.isZBatches) {
            this.productInfo = initProductInfo.copy()
            val taskRepository = taskManager.getReceivingTask()?.taskRepository
            currentProductDiscrepancies.clear()
            taskRepository
                    ?.getProductsDiscrepancies()
                    ?.findProductDiscrepanciesOfProduct(productInfo)
                    ?.mapTo(currentProductDiscrepancies) { it.copy() }

            this
        }
        else null
    }

}