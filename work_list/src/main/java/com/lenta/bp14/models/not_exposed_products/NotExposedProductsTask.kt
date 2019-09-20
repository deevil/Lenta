package com.lenta.bp14.models.not_exposed_products

import androidx.lifecycle.LiveData
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.models.general.TaskTypes
import com.lenta.bp14.models.not_exposed_products.repo.INotExposedProductInfo
import com.lenta.bp14.models.not_exposed_products.repo.INotExposedProductsRepo


class NotExposedProductsTask(
        private val taskDescription: NotExposedProductsTaskDescription,
        private val notExposedProductsRepo: INotExposedProductsRepo) : INotExposedProductsTask {
    override fun getTaskType(): ITaskType {
        return TaskTypes.NotExposedProducts.taskType
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

    override fun getProducts(): LiveData<List<INotExposedProductInfo>> {
        return notExposedProductsRepo.getProducts()
    }
}


interface INotExposedProductsTask : ITask {
    fun getProducts(): LiveData<List<INotExposedProductInfo>>


}