package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import javax.inject.Inject

@AppScope
class ProcessEditingInvoiceService
@Inject constructor() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var dataBase: IDataBaseRepo

    private lateinit var productInfo: TaskProductInfo

    fun newProcessGeneralProductService(productInfo: TaskProductInfo) : ProcessEditingInvoiceService? {
        return if (productInfo.type == ProductType.General){
            this.productInfo = productInfo.copy()
            this
        }
        else null
    }

}