package com.lenta.bp14.models.not_exposed_products

import androidx.lifecycle.LiveData
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.models.general.TaskTypes
import com.lenta.bp14.models.not_exposed_products.repo.INotExposedProductInfo
import com.lenta.bp14.models.not_exposed_products.repo.INotExposedProductsRepo
import com.lenta.bp14.models.not_exposed_products.repo.NotExposedProductInfo
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult


class NotExposedProductsTask(
        private val taskDescription: NotExposedProductsTaskDescription,
        private val notExposedProductsRepo: INotExposedProductsRepo) : INotExposedProductsTask {

    override var scanInfoResult: ScanInfoResult? = null


    override fun getTaskType(): ITaskType {
        return TaskTypes.NotExposedProducts.taskType
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

    override fun getProducts(): LiveData<List<INotExposedProductInfo>> {
        return notExposedProductsRepo.getProducts()
    }

    override fun getFilteredProducts(): LiveData<List<INotExposedProductInfo>> {
        return notExposedProductsRepo.getProducts()
    }

    override fun setCheckInfo(quantity: Double?, isEmptyPlaceMarked: Boolean?) {
        scanInfoResult.let {
            requireNotNull(it)
            notExposedProductsRepo.addOrReplaceProduct(
                    NotExposedProductInfo(
                            ean = null,
                            matNr = it.productInfo.materialNumber,
                            name = it.productInfo.description,
                            quantity = quantity,
                            uom = it.productInfo.uom.name,
                            isEmptyPlaceMarked = isEmptyPlaceMarked
                    )
            )
        }

    }

    override fun removeCheckResultsByMatNumbers(matNumbers: Set<String>) {
        notExposedProductsRepo.removeProducts(matNumbers)
    }

}


interface INotExposedProductsTask : ITask {

    var scanInfoResult: ScanInfoResult?

    fun getProducts(): LiveData<List<INotExposedProductInfo>>

    fun getFilteredProducts(): LiveData<List<INotExposedProductInfo>>

    fun setCheckInfo(quantity: Double?, isEmptyPlaceMarked: Boolean?)

    fun removeCheckResultsByMatNumbers(matNumbers: Set<String>)

}